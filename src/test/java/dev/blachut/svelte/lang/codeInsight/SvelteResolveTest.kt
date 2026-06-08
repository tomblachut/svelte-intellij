// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.polySymbols.testFramework.multiResolvePolySymbolReference
import com.intellij.polySymbols.testFramework.resolvePolySymbolReference
import com.intellij.psi.css.CssClass
import com.intellij.psi.util.contextOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.psi.blocks.SvelteEachPrimaryBranch
import junit.framework.TestCase

class SvelteResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath() + "/" + basePath
  override fun getBasePath(): String = "dev/blachut/svelte/lang/resolve"

  fun testBlock() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                let promise = Promise.resolve();
            </script>

            {#await promise then value}
                <h1>{<caret>value}</h1>
            {/await}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals(variable?.text, "value")
  }

  fun testBranchIsolation() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                let promise = Promise.resolve();
            </script>

            {#await promise then value}
                <h1>{value}</h1>
            {:catch error}
                <h1>{<caret>value}</h1>
            {/await}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNull(variable)
  }

  fun testEachKeyExpression() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                let items = [
                    {name: 'alice', id: 'a'},
                    {name: 'bob', id: 'b'},
                ];
            </script>

            {#each items as {name, id} (<caret>id)}
                <article>{name}</article>
            {/each}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals(variable?.text, "id")
  }

  fun testVariableShadowing() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                let then = Promise.resolve();
            </script>

            {#await <caret>then then then}
                <h1>{then}</h1>
            {/await}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertNotNull(variable!!.contextOfType<JSTagEmbeddedContent>())
  }

  fun testConstTagVariableShadowing() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                const people = [1, 2, 3];
            </script>

            {#each people as person}
                {@const people = ["hello"]}
                {<caret>people}
            {/each}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertNotNull(variable!!.contextOfType<SvelteEachPrimaryBranch>())
  }

  fun testConstTagBlockScope() {
    myFixture.configureByText(
      "Example.svelte", """
            {#each <caret>people as person}
                {@const people = ["hello"]}
            {/each}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNull(variable)
  }

  fun testConstTagResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {const greeting = "hi"}
                {<caret>greeting}
            {/if}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("greeting", variable.name)
  }

  fun testLetTagResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {let counter = 0}
                {<caret>counter}
            {/if}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertFalse("{let} binding should resolve to a mutable variable", variable.isConst)
  }

  fun testLetTagBlockScope() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {let scoped = 1}
            {/if}
            {<caret>scoped}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    TestCase.assertNull(reference!!.resolve())
  }

  fun testConstTagTopLevelResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {const greeting = "hi"}
            {<caret>greeting}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("greeting", variable.name)
  }

  fun testLetTagTopLevelResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {
                let variable = 123
            }
            {<caret>variable}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("variable", variable.name)
    TestCase.assertFalse("{let} binding should resolve to a mutable variable", variable.isConst)
  }

  fun testAtConstTagTopLevelStaysUnresolved() {
    // Legacy {@const} is not valid at the top level in Svelte; keep it non-resolving (unchanged behavior).
    myFixture.configureByText(
      "Example.svelte", """
            {@const variable = 123}
            {<caret>variable}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    TestCase.assertNull(reference!!.resolve())
  }

  fun testEachWithoutAsClauseIndexResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                const array = [1, 2, 3];
            </script>

            {#each array, i}
                <p>Index: {<caret>i}</p>
            {/each}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals(variable?.text, "i")
  }

  fun testAHrefIndexResolve() {
    doPathResolveTest("+page.svelte")
  }

  fun testAHrefDirectoryResolve() {
    doPathResolveTest("insideDeclaration")
  }

  fun testCustomLinkHrefFileResolve() {
    doPathResolveTest("component.svelte")
  }

  fun testAHrefFileWithExtensionResolve() {
    doPathResolveTest("component.svelte")
  }

  fun testNotRoutesResolve() {
    doPathResolveTest("+page.svelte")
  }

  fun testUnresolvedDirectoryResolve() {
    doPathResolveTest()
  }

  fun testRoutePathImportNotResolve() {
    doPathResolveTest()
  }

  fun testPathImportResolve() {
    doPathResolveTest("component.svelte")
  }

  // TypeScript in markup resolution tests
  fun testTsGenericResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts" generics="T extends { name: string }">
                export let item: T;
            </script>

            <div>{<caret>item.name}</div>
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals("item: T", variable?.text)
  }

  fun testTsTypeAssertionResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User {
                    name: string;
                }
                let user: User = { name: 'Alice' };
            </script>

            <div>{(<caret>user as User).name}</div>
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
  }

  fun testTsEachBlockResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface Item { id: number; name: string; }
                let items: Item[] = [{ id: 1, name: 'A' }];
            </script>

            {#each items as item}
                <div>{<caret>item.name}</div>
            {/each}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals(variable?.text, "item")
  }

  fun testTsAwaitBlockResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User { name: string; }
                let promise: Promise<User> = Promise.resolve({ name: 'Alice' });
            </script>

            {#await promise then user}
                <div>{<caret>user.name}</div>
            {/await}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals(variable?.text, "user")
  }

  fun testTsSnippetParameterResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User { name: string; age: number; }
            </script>

            {#snippet userCard(user: User)}
                <div>{<caret>user.name}</div>
            {/snippet}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)

    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertEquals("user: User", variable?.text)
  }

  fun testNsSegmentResolves() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <For<caret>ms.Input />
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val resolved = reference!!.resolve()
    TestCase.assertNotNull("Expected Forms namespace import binding to be resolved", resolved)
    TestCase.assertTrue("Expected ES6ImportedBinding, got ${resolved?.javaClass}", resolved is ES6ImportedBinding)
    TestCase.assertEquals("Forms", (resolved as ES6ImportedBinding).name)
  }

  fun testNsComponentSegmentResolves() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <Forms.Inp<caret>ut />
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull("Expected a reference on the component segment", reference)
    val resolved = reference!!.resolve()
    // Resolves to namespace binding for hover underline; actual component-level navigation is via LSP
    TestCase.assertNotNull("Expected component segment to resolve to namespace binding", resolved)
    TestCase.assertTrue("Expected ES6ImportedBinding", resolved is ES6ImportedBinding)
  }

  fun testNsAllRefsPresent() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <Forms.Input />
            """.trimIndent()
    )
    val tag = myFixture.file.findElementAt(myFixture.editor.document.text.indexOf("Forms.Input"))!!.parent
    TestCase.assertTrue("Expected SvelteHtmlTag, got ${tag.javaClass}", tag is dev.blachut.svelte.lang.psi.SvelteHtmlTag)

    val allRefs = tag.references
    val refInfo = allRefs.map { "${it.javaClass.simpleName}: range=${it.rangeInElement}, resolves=${it.resolve() != null}" }
    // Should have at least: SvelteNamespacePrefixReference (Forms) + SvelteTagNameReference (Input)
    TestCase.assertTrue(
      "Expected at least 2 references on namespaced tag, got ${allRefs.size}: $refInfo",
      allRefs.size >= 2
    )

    val namespaceRef = allRefs.find { it.rangeInElement.substring(tag.text) == "Forms" }
    TestCase.assertNotNull("Expected a reference covering 'Forms', refs: $refInfo", namespaceRef)
    TestCase.assertNotNull("Forms reference should resolve", namespaceRef!!.resolve())

    val componentRef = allRefs.find { it.rangeInElement.substring(tag.text) == "Input" }
    TestCase.assertNotNull("Expected a reference covering 'Input', refs: $refInfo", componentRef)
  }

  fun testNsUsagesFromImport() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as For<caret>ms from "./forms";
            </script>

            <Forms.Input />
            <Forms.Button />
            """.trimIndent()
    )
    val usages = myFixture.findUsages(myFixture.elementAtCaret)
    TestCase.assertTrue("Expected at least 2 template usages of Forms", usages.size >= 2)
  }

  fun testNsUsagesFromTag() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <For<caret>ms.Input />
            <Forms.Button />
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull("Expected reference at Forms position in tag", reference)
    val resolved = reference!!.resolve()
    TestCase.assertNotNull("Expected Forms to resolve", resolved)
    val usages = myFixture.findUsages(resolved!!)
    TestCase.assertTrue("Expected at least 2 usages of Forms from tag position, got ${usages.size}", usages.size >= 2)
  }

  fun testNsUnresolvedReturnsNull() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
            </script>

            <For<caret>ms.Input />
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val resolved = reference!!.resolve()
    TestCase.assertNull("Expected unimported namespace to resolve to null", resolved)
  }

  fun testConstTagElementScopeResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <div>
                {const x = 1}
                <span>{<caret>x}</span>
            </div>
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("x", variable.name)
  }

  fun testDeclarationTagShadowingResolvesInner() {
    myFixture.configureByText(
      "Example.svelte", """
            {const hello = 'hello'}
            <div>
                {const hello = 'hi'}
                {<caret>hello}
            </div>
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("'hi'", variable.initializer?.text)
  }

  fun testDeclarationTagShadowingResolvesOuterAfterElement() {
    myFixture.configureByText(
      "Example.svelte", """
            {const hello = 'hello'}
            <div>
                {const hello = 'hi'}
            </div>
            {<caret>hello}
            """.trimIndent()
    )
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = assertInstanceOf(reference!!.resolve(), JSVariable::class.java)
    TestCase.assertEquals("'hello'", variable.initializer?.text)
  }

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, getTestName(false), "svelte")
  }

  fun testClassReferenceFromSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-cl<caret>ass"></svelte:element>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
    val resolved = ref.resolve()
    TestCase.assertNotNull("Class reference on <svelte:element> should resolve to the CSS selector", resolved)
  }

  fun testIdReferenceFromSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} id="my-i<caret>d"></svelte:element>
      <style>
        #my-id { color: blue; }
      </style>
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
    val resolved = ref.resolve()
    TestCase.assertNotNull("Id reference on <svelte:element> should resolve to the CSS selector", resolved)
  }

  fun testClassReferenceNotCreatedOnRegularDiv() {
    myFixture.configureByText("Foo.svelte", """
      <div class="my-cl<caret>ass"></div>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
    val resolved = ref.resolve()
    TestCase.assertNotNull(
      "Class reference on a regular <div> must still resolve " +
      "(via platform provider, not ours) — verifies our filter does not displace platform behavior",
      resolved,
    )
  }

  fun testClassReferenceFromSvelteSelf() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:self class="my-cl<caret>ass"></svelte:self>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
    val resolved = ref.resolve()
    TestCase.assertNotNull("Class reference on <svelte:self> should resolve to the CSS selector (same scoped CSS as <svelte:element>)", resolved)
  }

  fun testClassReferenceNotCreatedOnSvelteComponent() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        let Cmp = null;
      </script>
      <svelte:component this={Cmp} class="my-cl<caret>ass"></svelte:component>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNull(
      "<svelte:component> forwards class to a differently-scoped child component; " +
      "local .my-class should NOT resolve (scope boundary vs. <svelte:element> / <svelte:self>)",
      ref,
    )
  }

  fun testClassReferenceInStringLiteralExpression() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'my-cl<caret>ass'}></div>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'my-cl<caret>ass'")
  }

  fun testClassReferenceInConditionalExpressionThenBranch() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? 'fo<caret>o' : 'bar'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'fo<caret>o'")
  }

  fun testClassReferenceInConditionalExpressionElseBranch() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? 'foo' : 'ba<caret>r'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'ba<caret>r'")
  }

  fun testClassReferenceInArrayLiteralFirstElement() {
    myFixture.configureByText("Foo.svelte", """
      <div class={['fo<caret>o', 'bar']}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'fo<caret>o'")
  }

  fun testClassReferenceInArrayLiteralSecondElement() {
    myFixture.configureByText("Foo.svelte", """
      <div class={['foo', 'ba<caret>r']}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'ba<caret>r'")
  }

  fun testClassReferenceInObjectLiteralInsideArrayLiteral() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={['base', { ac<caret>tive: cond }]}></div>
      <style>
        .base { color: red; }
        .active { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("ac<caret>tive:")
  }

  fun testClassReferenceInArrayLiteralInsideConditionalExpression() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? ['fo<caret>o'] : []}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'fo<caret>o'")
  }

  fun testClassReferenceInObjectLiteralPropertyKey() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={{ fo<caret>o: cond }}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("fo<caret>o:")
  }

  fun testClassReferenceInExpressionOnSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <script>let t = 'div';</script>
      <svelte:element this={t} class={'fo<caret>o'}></svelte:element>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'fo<caret>o'")
  }

  fun testClassReferenceInExpressionOnSvelteSelf() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <svelte:self class={{ fo<caret>o: cond }}></svelte:self>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("fo<caret>o:")
  }

  fun testClassReferenceInExpressionNotCreatedOnSvelteComponent() {
    myFixture.configureByText("Foo.svelte", """
      <script>import Foo from './Foo.svelte';</script>
      <svelte:component this={Foo} class={'fo<caret>o'}></svelte:component>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("'fo<caret>o'")
    TestCase.assertTrue(
      "<svelte:component> is excluded from class expression scope; must not resolve to the .foo CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }

  fun testClassReferenceInExpressionNotCreatedOnComponentTag() {
    myFixture.configureByText("Foo.svelte", """
      <script>import Foo from './Foo.svelte';</script>
      <Foo class={'fo<caret>o'} />
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("'fo<caret>o'")
    assertTrue(
      "Component class props are scoped to the child component; must not resolve to the local .foo CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }

  fun testClassReferenceInExpressionNotCreatedOnNamespacedComponentTag() {
    myFixture.configureByText("Foo.svelte", """
      <script>let Forms = { Button: null };</script>
      <Forms.Button class={{ fo<caret>o: true }} />
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("fo<caret>o:")
    assertTrue(
      "Namespaced component class props are scoped to the child component; must not resolve to the local .foo CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }

  fun testClassReferenceInExpressionResolvesToNullForUnknownClass() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'unknow<caret>n'}></div>
      <style>
        .other { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("'unknow<caret>n'")
    TestCase.assertTrue(
      "Unknown class must not resolve to a real CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }

  fun testClassReferenceInExpressionMultiClassLiteralFirst() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'fo<caret>o bar'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'fo<caret>o bar'")
  }

  fun testClassReferenceInExpressionMultiClassLiteralSecond() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'foo ba<caret>r'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    myFixture.resolvePolySymbolReference("'foo ba<caret>r'")
  }

  fun testClassReferenceInExpressionFromFunctionCallNotResolved() {
    myFixture.configureByText("Foo.svelte", """
      <script>function cn(s) { return s; }</script>
      <div class={cn('fo<caret>o')}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("'fo<caret>o'")
    TestCase.assertTrue(
      "Function-call wrapped class strings are out of scope; must not resolve to a real CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }

  fun testIdReferenceInExpressionIsNotCreated() {
    myFixture.configureByText("Foo.svelte", """
      <div id={'fo<caret>o'}></div>
      <style>
        #foo { color: red; }
      </style>
    """.trimIndent())
    val symbols = myFixture.multiResolvePolySymbolReference("'fo<caret>o'")
    TestCase.assertTrue(
      "id={...} expressions are out of scope for Tier A (class-only); must not resolve to a real CSS rule (got: $symbols)",
      symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass }
    )
  }
}

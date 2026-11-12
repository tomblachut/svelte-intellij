// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.polySymbols.testFramework.multiResolveSymbolReference
import com.intellij.polySymbols.testFramework.resolvePolySymbolReference
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.util.contextOfType
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.psi.blocks.SvelteEachPrimaryBranch
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class SvelteResolveTest {
  companion object {
    private const val BASE_PATH = "dev/blachut/svelte/lang/resolve"
  }

  private val projectFixture = svelteProjectFixture()
  private val myFixture by svelteCodeInsightFixture(projectFixture, getSvelteTestDataPath() + "/" + BASE_PATH)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("value", variable?.text)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNull(variable)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("id", variable?.text)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertNotNull(variable!!.contextOfType<JSTagEmbeddedContent>())
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertNotNull(variable!!.contextOfType<SvelteEachPrimaryBranch>())
    } }
  }

  @Test
  fun testConstTagBlockScope() {
    myFixture.configureByText(
      "Example.svelte", """
            {#each <caret>people as person}
                {@const people = ["hello"]}
            {/each}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNull(variable)
    } }
  }

  @Test
  fun testConstTagResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {const greeting = "hi"}
                {<caret>greeting}
            {/if}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("greeting", variable.name)
    } }
  }

  @Test
  fun testConstTagWithSemicolonResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {const greeting = "hi";}
                {<caret>greeting}
            {/if}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("greeting", variable.name)
    } }
  }

  @Test
  fun testLetTagResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {let counter = 0}
                {<caret>counter}
            {/if}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertFalse(variable.isConst, "{let} binding should resolve to a mutable variable")
    } }
  }

  @Test
  fun testLetTagBlockScope() {
    myFixture.configureByText(
      "Example.svelte", """
            {#if true}
                {let scoped = 1}
            {/if}
            {<caret>scoped}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      assertNull(reference!!.resolve())
    } }
  }

  @Test
  fun testConstTagTopLevelResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {const greeting = "hi"}
            {<caret>greeting}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("greeting", variable.name)
    } }
  }

  @Test
  fun testLetTagTopLevelResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            {
                let variable = 123
            }
            {<caret>variable}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("variable", variable.name)
      assertFalse(variable.isConst, "{let} binding should resolve to a mutable variable")
    } }
  }

  @Test
  fun testAtConstTagTopLevelStaysUnresolved() {
    // Legacy {@const} is not valid at the top level in Svelte; keep it non-resolving (unchanged behavior).
    myFixture.configureByText(
      "Example.svelte", """
            {@const variable = 123}
            {<caret>variable}
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      assertNull(reference!!.resolve())
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("i", variable?.text)
    } }
  }

  @Test
  fun testAHrefIndexResolve() {
    doPathResolveTest("+page.svelte")
  }

  @Test
  fun testAHrefDirectoryResolve() {
    doPathResolveTest("insideDeclaration")
  }

  @Test
  fun testCustomLinkHrefFileResolve() {
    doPathResolveTest("component.svelte")
  }

  @Test
  fun testAHrefFileWithExtensionResolve() {
    doPathResolveTest("component.svelte")
  }

  @Test
  fun testNotRoutesResolve() {
    doPathResolveTest("+page.svelte")
  }

  @Test
  fun testUnresolvedDirectoryResolve() {
    doPathResolveTest()
  }

  @Test
  fun testRoutePathImportNotResolve() {
    doPathResolveTest()
  }

  @Test
  fun testPathImportResolve() {
    doPathResolveTest("component.svelte")
  }

  // TypeScript in markup resolution tests
  @Test
  fun testTsGenericResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts" generics="T extends { name: string }">
                export let item: T;
            </script>

            <div>{<caret>item.name}</div>
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("item: T", variable?.text)
    } }
  }

  @Test
  fun testTsGenericConstraintImportResolve() {
    myFixture.addFileToProject("types.ts", "export interface Foo { name: string }")
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts" generics="T extends Foo">
                import type { Foo } from './types';
                export let item: T;
            </script>

            <div>{item}</div>
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val text = myFixture.file.text
      val offset = text.indexOf("Foo", text.indexOf("extends"))
      val reference = myFixture.file.findReferenceAt(offset)
      assertNotNull(reference, "No reference at the constraint 'Foo'")
      val resolved = reference!!.resolve()
      assertNotNull(resolved, "Constraint type 'Foo' should resolve to the imported symbol")
      val named = assertInstanceOf(PsiNamedElement::class.java, resolved)
      assertEquals(
        "Foo", named.name,
        "Constraint 'Foo' should resolve to the imported 'Foo' interface, not some other symbol"
      )
    } }
  }

  /**
   * A type declared in the `<script>` body is NOT visible from a generics constraint: svelte2tsx
   * compiles the script to `function render<T extends Base>() { ...body... }` and only hoists
   * imports above that function, so the Svelte language server reports "Base is not defined".
   * Only the legacy `$$Generic` syntax hoists body types. Keep the IDE in step with the compiler.
   */
  @Test
  fun testTsGenericConstraintLocalTypeDoesNotResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts" generics="T extends Base">
                interface Base { id: number }
                export let item: T;
            </script>

            <div>{item}</div>
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val text = myFixture.file.text
      val offset = text.indexOf("Base", text.indexOf("extends"))
      val reference = myFixture.file.findReferenceAt(offset)
      assertNotNull(reference, "No reference at the constraint 'Base'")
      assertNull(
        reference!!.resolve(),
        "A <script> body type must not resolve from a generics constraint - svelte2tsx does not hoist it"
      )
    } }
  }

  @Test
  fun testTsGenericConstraintUndeclaredStaysUnresolved() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts" generics="T extends Nonexistent">
                export let item: T;
            </script>

            <div>{item}</div>
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val text = myFixture.file.text
      val offset = text.indexOf("Nonexistent", text.indexOf("extends"))
      val reference = myFixture.file.findReferenceAt(offset)
      assertNotNull(reference, "No reference at the constraint 'Nonexistent'")
      assertNull(reference!!.resolve(), "Undeclared constraint type must not resolve")
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("item", variable?.text)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("user", variable?.text)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)

      val variable = reference!!.resolve()
      assertNotNull(variable)
      assertEquals("user: User", variable?.text)
    } }
  }

  @Test
  fun testNsSegmentResolves() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <For<caret>ms.Input />
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val resolved = reference!!.resolve()
      assertNotNull(resolved, "Expected Forms namespace import binding to be resolved")
      assertTrue(resolved is ES6ImportedBinding, "Expected ES6ImportedBinding, got ${resolved?.javaClass}")
      assertEquals("Forms", (resolved as ES6ImportedBinding).name)
    } }
  }

  @Test
  fun testNsComponentSegmentResolves() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <Forms.Inp<caret>ut />
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference, "Expected a reference on the component segment")
      val resolved = reference!!.resolve()
      // Resolves to namespace binding for hover underline; actual component-level navigation is via LSP
      assertNotNull(resolved, "Expected component segment to resolve to namespace binding")
      assertTrue(resolved is ES6ImportedBinding, "Expected ES6ImportedBinding")
    } }
  }

  @Test
  fun testNsAllRefsPresent() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <Forms.Input />
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val tag = myFixture.file.findElementAt(myFixture.editor.document.text.indexOf("Forms.Input"))!!.parent
      assertTrue(tag is dev.blachut.svelte.lang.psi.SvelteHtmlTag, "Expected SvelteHtmlTag, got ${tag.javaClass}")

      val allRefs = tag.references
      val refInfo = allRefs.map { "${it.javaClass.simpleName}: range=${it.rangeInElement}, resolves=${it.resolve() != null}" }
      // Should have at least: SvelteNamespacePrefixReference (Forms) + SvelteTagNameReference (Input)
      assertTrue(
        allRefs.size >= 2,
        "Expected at least 2 references on namespaced tag, got ${allRefs.size}: $refInfo"
      )

      val namespaceRef = allRefs.find { it.rangeInElement.substring(tag.text) == "Forms" }
      assertNotNull(namespaceRef, "Expected a reference covering 'Forms', refs: $refInfo")
      assertNotNull(namespaceRef!!.resolve(), "Forms reference should resolve")

      val componentRef = allRefs.find { it.rangeInElement.substring(tag.text) == "Input" }
      assertNotNull(componentRef, "Expected a reference covering 'Input', refs: $refInfo")
    } }
  }

  @Test
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
    val usages = myFixture.findUsages(runReadActionBlocking { myFixture.elementAtCaret })
    assertTrue(usages.size >= 2, "Expected at least 2 template usages of Forms")
  }

  @Test
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
    assertNotNull(reference, "Expected reference at Forms position in tag")
    val resolved = timeoutRunBlocking { readAction { reference!!.resolve() } }
    assertNotNull(resolved, "Expected Forms to resolve")
    val usages = myFixture.findUsages(resolved!!)
    assertTrue(usages.size >= 2, "Expected at least 2 usages of Forms from tag position, got ${usages.size}")
  }

  @Test
  fun testNsUnresolvedReturnsNull() {
    myFixture.configureByText(
      "Example.svelte", """
            <script>
            </script>

            <For<caret>ms.Input />
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val resolved = reference!!.resolve()
      assertNull(resolved, "Expected unimported namespace to resolve to null")
    } }
  }

  @Test
  fun testConstTagElementScopeResolve() {
    myFixture.configureByText(
      "Example.svelte", """
            <div>
                {const x = 1}
                <span>{<caret>x}</span>
            </div>
            """.trimIndent()
    )
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("x", variable.name)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("'hi'", variable.initializer?.text)
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val reference = myFixture.getReferenceAtCaretPosition()
      assertNotNull(reference)
      val variable = assertInstanceOf(JSVariable::class.java, reference!!.resolve())
      assertEquals("'hello'", variable.initializer?.text)
    } }
  }

  private fun doPathResolveTest(destination: String? = null) {
    runInEdtAndWait {
      checkResolveToDestination(destination, myFixture, testName, "svelte")
    }
  }

  @Test
  fun testClassReferenceFromSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-cl<caret>ass"></svelte:element>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
      val resolved = ref.resolve()
      assertNotNull(resolved, "Class reference on <svelte:element> should resolve to the CSS selector")
    } }
  }

  @Test
  fun testIdReferenceFromSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} id="my-i<caret>d"></svelte:element>
      <style>
        #my-id { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
      val resolved = ref.resolve()
      assertNotNull(resolved, "Id reference on <svelte:element> should resolve to the CSS selector")
    } }
  }

  @Test
  fun testClassReferenceNotCreatedOnRegularDiv() {
    myFixture.configureByText("Foo.svelte", """
      <div class="my-cl<caret>ass"></div>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
      val resolved = ref.resolve()
      assertNotNull(
        resolved,
        "Class reference on a regular <div> must still resolve " +
        "(via platform provider, not ours) — verifies our filter does not displace platform behavior",
      )
    } }
  }

  @Test
  fun testClassReferenceFromSvelteSelf() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:self class="my-cl<caret>ass"></svelte:self>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
      val resolved = ref.resolve()
      assertNotNull(resolved, "Class reference on <svelte:self> should resolve to the CSS selector (same scoped CSS as <svelte:element>)")
    } }
  }

  @Test
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
    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPosition()
      assertNull(
        ref,
        "<svelte:component> forwards class to a differently-scoped child component; " +
        "local .my-class should NOT resolve (scope boundary vs. <svelte:element> / <svelte:self>)",
      )
    } }
  }

  @Test
  fun testClassReferenceInStringLiteralExpression() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'my-cl<caret>ass'}></div>
      <style>
        .my-class { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'my-cl<caret>ass'")
    } }
  }

  @Test
  fun testClassReferenceInConditionalExpressionThenBranch() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? 'fo<caret>o' : 'bar'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'fo<caret>o'")
    } }
  }

  @Test
  fun testClassReferenceInConditionalExpressionElseBranch() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? 'foo' : 'ba<caret>r'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'ba<caret>r'")
    } }
  }

  @Test
  fun testClassReferenceInArrayLiteralFirstElement() {
    myFixture.configureByText("Foo.svelte", """
      <div class={['fo<caret>o', 'bar']}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'fo<caret>o'")
    } }
  }

  @Test
  fun testClassReferenceInArrayLiteralSecondElement() {
    myFixture.configureByText("Foo.svelte", """
      <div class={['foo', 'ba<caret>r']}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'ba<caret>r'")
    } }
  }

  @Test
  fun testClassReferenceInObjectLiteralInsideArrayLiteral() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={['base', { ac<caret>tive: cond }]}></div>
      <style>
        .base { color: red; }
        .active { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("ac<caret>tive:")
    } }
  }

  @Test
  fun testClassReferenceInArrayLiteralInsideConditionalExpression() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={cond ? ['fo<caret>o'] : []}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'fo<caret>o'")
    } }
  }

  @Test
  fun testClassReferenceInObjectLiteralPropertyKey() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <div class={{ fo<caret>o: cond }}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("fo<caret>o:")
    } }
  }

  @Test
  fun testClassReferenceInExpressionOnSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <script>let t = 'div';</script>
      <svelte:element this={t} class={'fo<caret>o'}></svelte:element>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'fo<caret>o'")
    } }
  }

  @Test
  fun testClassReferenceInExpressionOnSvelteSelf() {
    myFixture.configureByText("Foo.svelte", """
      <script>let cond = true;</script>
      <svelte:self class={{ fo<caret>o: cond }}></svelte:self>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("fo<caret>o:")
    } }
  }

  @Test
  fun testClassReferenceInExpressionNotCreatedOnSvelteComponent() {
    myFixture.configureByText("Foo.svelte", """
      <script>import Foo from './Foo.svelte';</script>
      <svelte:component this={Foo} class={'fo<caret>o'}></svelte:component>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("'fo<caret>o'")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "<svelte:component> is excluded from class expression scope; must not resolve to the .foo CSS rule (got: $symbols)"
      )
    } }
  }

  @Test
  fun testClassReferenceInExpressionNotCreatedOnComponentTag() {
    myFixture.configureByText("Foo.svelte", """
      <script>import Foo from './Foo.svelte';</script>
      <Foo class={'fo<caret>o'} />
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("'fo<caret>o'")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "Component class props are scoped to the child component; must not resolve to the local .foo CSS rule (got: $symbols)"
      )
    } }
  }

  @Test
  fun testClassReferenceInExpressionNotCreatedOnNamespacedComponentTag() {
    myFixture.configureByText("Foo.svelte", """
      <script>let Forms = { Button: null };</script>
      <Forms.Button class={{ fo<caret>o: true }} />
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("fo<caret>o:")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "Namespaced component class props are scoped to the child component; must not resolve to the local .foo CSS rule (got: $symbols)"
      )
    } }
  }

  @Test
  fun testClassReferenceInExpressionResolvesToNullForUnknownClass() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'unknow<caret>n'}></div>
      <style>
        .other { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("'unknow<caret>n'")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "Unknown class must not resolve to a real CSS rule (got: $symbols)"
      )
    } }
  }

  @Test
  fun testClassReferenceInExpressionMultiClassLiteralFirst() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'fo<caret>o bar'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'fo<caret>o bar'")
    } }
  }

  @Test
  fun testClassReferenceInExpressionMultiClassLiteralSecond() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'foo ba<caret>r'}></div>
      <style>
        .foo { color: red; }
        .bar { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      myFixture.resolvePolySymbolReference("'foo ba<caret>r'")
    } }
  }

  @Test
  fun testClassReferenceInExpressionFromFunctionCallNotResolved() {
    myFixture.configureByText("Foo.svelte", """
      <script>function cn(s) { return s; }</script>
      <div class={cn('fo<caret>o')}></div>
      <style>
        .foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("'fo<caret>o'")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "Function-call wrapped class strings are out of scope; must not resolve to a real CSS rule (got: $symbols)"
      )
    } }
  }

  @Test
  fun testIdReferenceInExpressionIsNotCreated() {
    myFixture.configureByText("Foo.svelte", """
      <div id={'fo<caret>o'}></div>
      <style>
        #foo { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { readAction {
      val symbols = myFixture.multiResolveSymbolReference("'fo<caret>o'")
      assertTrue(
        symbols.none { (it as? PsiLinkedPolySymbol)?.linkedElement is CssClass },
        "id={...} expressions are out of scope for Tier A (class-only); must not resolve to a real CSS rule (got: $symbols)"
      )
    } }
  }
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
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

  fun testNsFileUsages() {
    // Set up the component file and module structure
    val buttonComponent = myFixture.configureByText(
      "Button.svelte", """
            <script>
                export let label = "";
            </script>
            <button>{label}</button>
            """.trimIndent()
    )
    myFixture.configureByText(
      "index.js", """
            export { default as Button } from "./Button.svelte";
            """.trimIndent()
    )
    myFixture.configureByText(
      "Example.svelte", """
            <script>
                import * as UI from "./index.js";
            </script>

            <UI.Button label="Click me" />
            """.trimIndent()
    )

    // Find usages of the Button.svelte component file
    val usages = myFixture.findUsages(buttonComponent)
    val usageFiles = usages.mapNotNull { it.element?.containingFile?.name }
    // Should find the re-export in index.js
    TestCase.assertTrue(
      "Expected usage in index.js, got: $usageFiles",
      usageFiles.contains("index.js")
    )
    // Should find the <UI.Button> template usage in Example.svelte
    TestCase.assertTrue(
      "Expected template usage in Example.svelte (<UI.Button>), got: $usageFiles",
      usageFiles.contains("Example.svelte")
    )
  }

  fun testNsTemplateUsage() {
    // Button.svelte — the component we'll search usages for
    myFixture.configureByText("Button.svelte", """
      <script>
        export let variant = "default";
      </script>
      <button class={variant}><slot /></button>
    """.trimIndent())

    // Card.svelte — another component in the same namespace
    myFixture.configureByText("Card.svelte", """
      <div class="card"><slot /></div>
    """.trimIndent())

    // index.ts — re-exports both components as a namespace
    myFixture.addFileToProject("lib/index.ts", """
      export { default as Button } from '../Button.svelte';
      export { default as Card } from '../Card.svelte';
    """.trimIndent())

    // Consumer.svelte — uses Button via namespace <UI.Button>
    myFixture.configureByText("Consumer.svelte", """
      <script>
        import * as UI from './lib/index';
      </script>
      <UI.Card>
        <UI.Button variant="primary">Click me</UI.Button>
      </UI.Card>
    """.trimIndent())

    // Find usages of Button.svelte — should include the <UI.Button> template usage
    val buttonFile = myFixture.findFileInTempDir("Button.svelte")!!
    val buttonPsi = com.intellij.psi.PsiManager.getInstance(project).findFile(buttonFile)!!
    val usages = myFixture.findUsages(buttonPsi)
    val usageFiles = usages.mapNotNull { it.element?.containingFile?.name }

    TestCase.assertTrue(
      "Expected re-export usage in index.ts, got: $usageFiles",
      usageFiles.contains("index.ts")
    )
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usageFiles",
      usageFiles.contains("Consumer.svelte")
    )
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

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, getTestName(false), "svelte")
  }
}

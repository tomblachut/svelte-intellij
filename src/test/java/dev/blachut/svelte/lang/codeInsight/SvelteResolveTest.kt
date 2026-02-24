// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

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

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, getTestName(false), "svelte")
  }
}

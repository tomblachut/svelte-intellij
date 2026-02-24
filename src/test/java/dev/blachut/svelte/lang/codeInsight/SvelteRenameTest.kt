package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSAbstractRenameTest
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteRenameTest : JSAbstractRenameTest() {
  override fun getTestDataPath(): String = getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/rename"

  fun testStoreSubscriptionJS() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.js")
  }

  fun testStoreSubscriptionTS() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.ts")
  }

  fun testStoreDeclaration() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.ts", "${name}JS.svelte", "${name}TS.svelte")
  }

  fun testRenameGeneric() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("Entity", "$name.svelte")
  }

  // TypeScript in markup rename tests
  fun testTsVariableRenameInExpression() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                let <caret>count: number = 0;
            </script>

            <div>{count}</div>
            <button on:click={() => count++}>Increment</button>
            """.trimIndent()
    )
    myFixture.renameElementAtCaret("total")
    myFixture.checkResult("""
            <script lang="ts">
                let total: number = 0;
            </script>

            <div>{total}</div>
            <button on:click={() => total++}>Increment</button>
            """.trimIndent())
  }

  fun testTsInterfacePropertyRename() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User {
                    <caret>name: string;
                }
                let user: User = { name: 'Alice' };
            </script>

            <div>{user.name}</div>
            """.trimIndent()
    )
    myFixture.renameElementAtCaret("fullName")
    myFixture.checkResult("""
            <script lang="ts">
                interface User {
                    fullName: string;
                }
                let user: User = { fullName: 'Alice' };
            </script>

            <div>{user.fullName}</div>
            """.trimIndent())
  }

  fun testTsEachBlockVariableRename() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface Item { id: number; }
                let items: Item[] = [{ id: 1 }];
            </script>

            {#each items as <caret>item}
                <div>{item.id}</div>
            {/each}
            """.trimIndent()
    )
    myFixture.renameElementAtCaret("element")
    myFixture.checkResult("""
            <script lang="ts">
                interface Item { id: number; }
                let items: Item[] = [{ id: 1 }];
            </script>

            {#each items as element}
                <div>{element.id}</div>
            {/each}
            """.trimIndent())
  }

  fun testTsAwaitBlockVariableRename() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User { name: string; }
                let promise: Promise<User> = Promise.resolve({ name: 'Alice' });
            </script>

            {#await promise then <caret>user}
                <div>{user.name}</div>
            {/await}
            """.trimIndent()
    )
    myFixture.renameElementAtCaret("data")
    myFixture.checkResult("""
            <script lang="ts">
                interface User { name: string; }
                let promise: Promise<User> = Promise.resolve({ name: 'Alice' });
            </script>

            {#await promise then data}
                <div>{data.name}</div>
            {/await}
            """.trimIndent())
  }

  fun testTsSnippetParameterRename() {
    myFixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User { name: string; }
            </script>

            {#snippet userCard(<caret>user: User)}
                <div>{user.name}</div>
            {/snippet}
            """.trimIndent()
    )
    myFixture.renameElementAtCaret("person")
    myFixture.checkResult("""
            <script lang="ts">
                interface User { name: string; }
            </script>

            {#snippet userCard(person: User)}
                <div>{person.name}</div>
            {/snippet}
            """.trimIndent())
  }
}
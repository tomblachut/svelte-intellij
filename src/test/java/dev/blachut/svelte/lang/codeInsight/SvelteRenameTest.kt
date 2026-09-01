package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSAbstractRenameTest
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
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

  /**
   * WEB-57512: a rename of a package component's import must carry the template tag with it. That
   * needs the reference search to report `<Fa />` as a usage of the binding.
   */
  fun testPackageComponentRename() {
    myFixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import <caret>Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())
    myFixture.renameElementAtCaret("FaIcon")
    myFixture.checkResult("""
      <script lang="ts">
        import FaIcon from 'svelte-fa';
      </script>

      <FaIcon />
    """.trimIndent())
  }

  /**
   * The name-match fallback serves the import only. A rename of the same-named `{#each}` variable
   * must leave the import's tag alone.
   */
  fun testEachVariableRenameKeepsTag() {
    myFixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';
        const items: string[] = [];
      </script>

      <Fa />

      {#each items as <caret>Fa}
        <span>{Fa}</span>
      {/each}
    """.trimIndent())
    myFixture.renameElementAtCaret("Item")
    myFixture.checkResult("""
      <script lang="ts">
        import Fa from 'svelte-fa';
        const items: string[] = [];
      </script>

      <Fa />

      {#each items as Item}
        <span>{Item}</span>
      {/each}
    """.trimIndent())
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

  fun testRenameClassFromSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    myFixture.renameElementAtCaret("renamed-class")
    myFixture.checkResult("""
      <svelte:element this={'div'} class="renamed-class"></svelte:element>
      <style>
        .renamed-class { color: red; }
      </style>
    """.trimIndent())
  }

  fun testRenameClassFromSvelteElementWithMultipleClasses() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class other-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
    myFixture.renameElementAtCaret("renamed-class")
    myFixture.checkResult("""
      <svelte:element this={'div'} class="renamed-class other-class"></svelte:element>
      <style>
        .renamed-class { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
  }

  fun testRenameClassFromCssSelectorToExpression() {
    myFixture.configureByText("Foo.svelte", """
      <div class={'my-class'}></div>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    myFixture.renameElementAtCaret("renamed-class")
    myFixture.checkResult("""
      <div class={'renamed-class'}></div>
      <style>
        .renamed-class { color: red; }
      </style>
    """.trimIndent())
  }
}
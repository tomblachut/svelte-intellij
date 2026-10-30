package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSRenameFixture.Companion.jsRenameFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.getSvelteTestDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

@TestApplication
class SvelteRenameTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsRenameFixture(
    projectFixture,
    JSFixtureOptions(getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/rename", "js"),
  )

  @Test
  fun testStoreSubscriptionJS() {
    val name = js.getTestName(false)
    js.doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.js")
  }

  @Test
  fun testStoreSubscriptionTS() {
    val name = js.getTestName(false)
    js.doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.ts")
  }

  @Test
  fun testStoreDeclaration() {
    val name = js.getTestName(false)
    js.doTestForFilesWithCheckAll("betterCount", "$name.ts", "${name}JS.svelte", "${name}TS.svelte")
  }

  @Test
  fun testRenameGeneric() {
    val name = js.getTestName(false)
    js.doTestForFilesWithCheckAll("Entity", "$name.svelte")
  }

  /**
   * WEB-57512: a rename of a package component's import must carry the template tag with it. That
   * needs the reference search to report `<Fa />` as a usage of the binding.
   */
  @Test
  fun testPackageComponentRename() {
    js.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    js.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import <caret>Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("FaIcon") } }
    js.fixture.checkResult("""
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
  @Test
  fun testEachVariableRenameKeepsTag() {
    js.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    js.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';
        const items: string[] = [];
      </script>

      <Fa />

      {#each items as <caret>Fa}
        <span>{Fa}</span>
      {/each}
    """.trimIndent())
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("Item") } }
    js.fixture.checkResult("""
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
  @Test
  fun testTsVariableRenameInExpression() {
    js.fixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                let <caret>count: number = 0;
            </script>

            <div>{count}</div>
            <button on:click={() => count++}>Increment</button>
            """.trimIndent()
    )
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("total") } }
    js.fixture.checkResult("""
            <script lang="ts">
                let total: number = 0;
            </script>

            <div>{total}</div>
            <button on:click={() => total++}>Increment</button>
            """.trimIndent())
  }

  @Test
  fun testTsInterfacePropertyRename() {
    js.fixture.configureByText(
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
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("fullName") } }
    js.fixture.checkResult("""
            <script lang="ts">
                interface User {
                    fullName: string;
                }
                let user: User = { fullName: 'Alice' };
            </script>

            <div>{user.fullName}</div>
            """.trimIndent())
  }

  @Test
  fun testTsEachBlockVariableRename() {
    js.fixture.configureByText(
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
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("element") } }
    js.fixture.checkResult("""
            <script lang="ts">
                interface Item { id: number; }
                let items: Item[] = [{ id: 1 }];
            </script>

            {#each items as element}
                <div>{element.id}</div>
            {/each}
            """.trimIndent())
  }

  @Test
  fun testTsAwaitBlockVariableRename() {
    js.fixture.configureByText(
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
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("data") } }
    js.fixture.checkResult("""
            <script lang="ts">
                interface User { name: string; }
                let promise: Promise<User> = Promise.resolve({ name: 'Alice' });
            </script>

            {#await promise then data}
                <div>{data.name}</div>
            {/await}
            """.trimIndent())
  }

  @Test
  fun testTsSnippetParameterRename() {
    js.fixture.configureByText(
      "Example.svelte", """
            <script lang="ts">
                interface User { name: string; }
            </script>

            {#snippet userCard(<caret>user: User)}
                <div>{user.name}</div>
            {/snippet}
            """.trimIndent()
    )
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("person") } }
    js.fixture.checkResult("""
            <script lang="ts">
                interface User { name: string; }
            </script>

            {#snippet userCard(person: User)}
                <div>{person.name}</div>
            {/snippet}
            """.trimIndent())
  }

  @Test
  fun testRenameClassFromSvelteElement() {
    js.fixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("renamed-class") } }
    js.fixture.checkResult("""
      <svelte:element this={'div'} class="renamed-class"></svelte:element>
      <style>
        .renamed-class { color: red; }
      </style>
    """.trimIndent())
  }

  @Test
  fun testRenameClassFromSvelteElementWithMultipleClasses() {
    js.fixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class other-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("renamed-class") } }
    js.fixture.checkResult("""
      <svelte:element this={'div'} class="renamed-class other-class"></svelte:element>
      <style>
        .renamed-class { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
  }

  @Test
  fun testRenameClassFromCssSelectorToExpression() {
    js.fixture.configureByText("Foo.svelte", """
      <div class={'my-class'}></div>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.renameElementAtCaret("renamed-class") } }
    js.fixture.checkResult("""
      <div class={'renamed-class'}></div>
      <style>
        .renamed-class { color: red; }
      </style>
    """.trimIndent())
  }
}
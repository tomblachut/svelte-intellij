package dev.blachut.svelte.lang.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.SvelteTestScriptLang
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteSelectWordTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
  fun testStoreSubscriptionAfterDollarJS(): Unit = storeSubscriptionAfterDollar.perform(SvelteTestScriptLang.JS)

  @Test
  fun testStoreSubscriptionAfterDollarTS(): Unit = storeSubscriptionAfterDollar.perform(SvelteTestScriptLang.TS)

  private val storeSubscriptionAfterDollar = SvelteTestScenario { langExt, _ ->
    val storedCount = "\$storedCount" // to trick Kotlin
    doWordSelectionTest(myFixture, """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        $<caret>storedCount.toFixed();
      </script>
    """.trimIndent(), """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        $<selection>storedCount</selection>.toFixed();
      </script>
    """.trimIndent(), """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        <selection>$storedCount</selection>.toFixed();
      </script>
    """.trimIndent(), """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        <selection>$storedCount.toFixed()</selection>;
      </script>
    """.trimIndent())
  }

  @Test
  fun testStoreSubscriptionBeforeIdentifierJS(): Unit = storeSubscriptionBeforeIdentifier.perform(SvelteTestScriptLang.JS)

  @Test
  fun testStoreSubscriptionBeforeIdentifierTS(): Unit = storeSubscriptionBeforeIdentifier.perform(SvelteTestScriptLang.TS)

  private val storeSubscriptionBeforeIdentifier = SvelteTestScenario { langExt, _ ->
    val storedCount = "\$storedCount" // to trick Kotlin
    doWordSelectionTest(myFixture, """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        <caret>$storedCount.toFixed();
      </script>
    """.trimIndent(), """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        <selection>$storedCount</selection>.toFixed();
      </script>
    """.trimIndent(), """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const storedCount = writable(5);
        <selection>$storedCount.toFixed()</selection>;
      </script>
    """.trimIndent())
  }

  /**
   * Adapted from [com.intellij.testFramework.fixtures.CodeInsightTestUtil.doWordSelectionTest]
   */
  private fun doWordSelectionTest(fixture: CodeInsightTestFixture,
                                  before: String,
                                  vararg after: String) {
    runInEdtAndWait {
      assert(after.isNotEmpty())
      fixture.configureByText("Foo.svelte", before)
      for (fileText in after) {
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_SELECT_WORD_AT_CARET)
        fixture.checkResult(fileText, false)
      }
    }
  }
}

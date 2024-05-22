package dev.blachut.svelte.lang.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.doTestWithLangFromTestNameSuffix

class SvelteSelectWordTest : BasePlatformTestCase() {
  fun testStoreSubscriptionAfterDollarJS() = doTestWithLangFromTestNameSuffix(storeSubscriptionAfterDollar)

  fun testStoreSubscriptionAfterDollarTS() = doTestWithLangFromTestNameSuffix(storeSubscriptionAfterDollar)

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

  fun testStoreSubscriptionBeforeIdentifierJS() = doTestWithLangFromTestNameSuffix(storeSubscriptionBeforeIdentifier)

  fun testStoreSubscriptionBeforeIdentifierTS() = doTestWithLangFromTestNameSuffix(storeSubscriptionBeforeIdentifier)

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

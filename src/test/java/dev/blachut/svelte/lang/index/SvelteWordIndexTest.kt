package dev.blachut.svelte.lang.index

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class SvelteWordIndexTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  // WEB-77758: tag-header comments must index as IN_COMMENTS like any other comment,
  // otherwise "Find Usages in comments" misses commented-out attributes.
  @Test
  fun testTagHeaderCommentsIndexedAsComments() {
    myFixture.configureByText("Foo.svelte", """
      <div
          // linecommentword={456}
          /* blockcommentword */
      >x</div>
      <!-- htmlcommentword -->
    """.trimIndent())

    assertTrue(foundInComments("linecommentword"), "line comment word not found in comments")
    assertTrue(foundInComments("blockcommentword"), "block comment word not found in comments")
    assertTrue(foundInComments("htmlcommentword"), "HTML comment word not found in comments") // pre-existing behavior
    assertFalse(foundInComments("x"), "attribute name found in comments") // content stays outside IN_COMMENTS
  }

  private fun foundInComments(word: String): Boolean {
    var found = false
    runReadActionBlocking {
      PsiSearchHelper.getInstance(myFixture.project).processElementsWithWord(
        { _, _ -> found = true; false },
        GlobalSearchScope.fileScope(myFixture.file),
        word,
        UsageSearchContext.IN_COMMENTS,
        true,
      )
    }
    return found
  }
}

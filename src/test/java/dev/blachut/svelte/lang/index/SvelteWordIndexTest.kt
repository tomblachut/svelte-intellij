package dev.blachut.svelte.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteWordIndexTest : BasePlatformTestCase() {
  // WEB-77758: tag-header comments must index as IN_COMMENTS like any other comment,
  // otherwise "Find Usages in comments" misses commented-out attributes.
  fun testTagHeaderCommentsIndexedAsComments() {
    myFixture.configureByText("Foo.svelte", """
      <div
          // linecommentword={456}
          /* blockcommentword */
      >x</div>
      <!-- htmlcommentword -->
    """.trimIndent())

    assertTrue("line comment word not found in comments", foundInComments("linecommentword"))
    assertTrue("block comment word not found in comments", foundInComments("blockcommentword"))
    assertTrue("HTML comment word not found in comments", foundInComments("htmlcommentword")) // pre-existing behavior
    assertFalse("attribute name found in comments", foundInComments("x")) // content stays outside IN_COMMENTS
  }

  private fun foundInComments(word: String): Boolean {
    var found = false
    PsiSearchHelper.getInstance(project).processElementsWithWord(
      { _, _ -> found = true; false },
      GlobalSearchScope.fileScope(myFixture.file),
      word,
      UsageSearchContext.IN_COMMENTS,
      true,
    )
    return found
  }
}

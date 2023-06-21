package dev.blachut.svelte.lang.service

import com.intellij.platform.lsp.tests.checkLspHighlighting
import dev.blachut.svelte.lang.checkCompletionContains
import dev.blachut.svelte.lang.getRelativeSvelteTestDataPath
import org.junit.Test

class SvelteServiceCompletionTest : SvelteServiceTestBase() {
  override fun getBasePath(): String = getRelativeSvelteTestDataPath() + "/dev/blachut/svelte/lang/service/completion"

  @Test
  fun testKitDataProp() {
    defaultCompletionTest(directory = true)
    val lookupElements = myFixture.completeBasic()
    lookupElements.checkCompletionContains("somePost", "uniqueName")
  }

  private fun defaultCompletionTest(directory: Boolean = false) {
    configureDefault(directory)

    myFixture.checkLspHighlighting()
    assertCorrectService()
  }
}

package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import dev.blachut.svelte.lang.getRelativeSvelteTestDataPath
import org.junit.Test

/**
 * @see com.intellij.lang.javascript.typescript.service.TypeScriptServiceDocumentationTest
 */
class SvelteServiceDocumentationTest : SvelteServiceTestBase() {
  override fun getBasePath(): String = getRelativeSvelteTestDataPath() + "/dev/blachut/svelte/lang/service/documentation"

  override fun setUp() {
    super.setUp()
    RegistryManager.getInstance().get("typescript.show.own.type").setValue(true, testRootDisposable)
  }

  @Test
  fun testNullChecks() = defaultQuickNavigateTest()

  @Test
  fun testTypeNarrowing() = defaultQuickNavigateTest()

  @Test
  fun testGenericType() = defaultQuickNavigateTest()

  @Test
  fun testQualifiedReference() = defaultQuickNavigateTest()

  private fun defaultQuickNavigateTest(directory: Boolean = false) {
    myFixture.configureByText("tsconfig.json", tsconfig)
    myFixture.configureByFile(getTestName(false) + "." + extension)
    myFixture.checkLspHighlighting()
    assertCorrectService()

    val doc = JSAbstractDocumentationTest.getQuickNavigateText(myFixture)
    JSAbstractDocumentationTest.checkExpected(doc, testDataPath + "/" + getTestName(false) + ".expected.html")
  }
}

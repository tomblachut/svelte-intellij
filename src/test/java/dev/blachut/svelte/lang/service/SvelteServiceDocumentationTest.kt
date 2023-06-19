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
  fun testNullChecks() = defaultComboTest()

  @Test
  fun testTypeNarrowing() = defaultComboTest()

  @Test
  fun testGenericType() = defaultComboTest()

  @Test
  fun testQualifiedReference() = defaultComboTest()

  @Test
  fun testQualifiedReference2() = defaultComboTest()

  @Test
  fun testNoDummyResolve() = defaultComboTest()

  @Test
  fun testNoDummyResolve2() = defaultComboTest()

  private fun defaultComboTest() {
    myFixture.configureByText("tsconfig.json", tsconfig)
    myFixture.configureByFile(getTestName(false) + "." + extension)
    myFixture.checkLspHighlighting()
    assertCorrectService()

    val quickNavigateText = JSAbstractDocumentationTest.getQuickNavigateText(myFixture)
    JSAbstractDocumentationTest.checkExpected(quickNavigateText, "$testFileAbsolutePathWithoutExtension.nav.expected.html")

    val doc = JSAbstractDocumentationTest.getQuickDocumentationText(myFixture) ?: "No documentation found."
    JSAbstractDocumentationTest.checkExpected(doc, "$testFileAbsolutePathWithoutExtension.doc.expected.html")
  }

  private val testFileAbsolutePathWithoutExtension
    get() = testDataPath + "/" + getTestName(false)
}

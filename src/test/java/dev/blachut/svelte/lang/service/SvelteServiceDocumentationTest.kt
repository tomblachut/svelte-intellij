package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
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

  @Test
  fun testKitDataProp() = defaultComboTest(true)

  private fun defaultComboTest(directory: Boolean = false) {
    configureDefault(directory)

    waitUntilFileOpenedByLspServer(project, file.virtualFile)
    assertCorrectService()

    val quickNavigateText = JSAbstractDocumentationTest.getQuickNavigateText(myFixture)
    JSAbstractDocumentationTest.checkExpected(quickNavigateText, "$testFileAbsolutePathWithoutExtension.nav.expected.html")

    val doc = JSAbstractDocumentationTest.getQuickDocumentationText(myFixture) ?: "No documentation found."
    JSAbstractDocumentationTest.checkExpected(doc, "$testFileAbsolutePathWithoutExtension.doc.expected.html")
  }

  private val testFileAbsolutePathWithoutExtension
    get() = testDataPath + "/" + getTestName(false)
}

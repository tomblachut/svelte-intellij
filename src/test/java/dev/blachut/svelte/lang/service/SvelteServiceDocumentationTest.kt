package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitForDiagnosticsDataFromServer
import dev.blachut.svelte.lang.getRelativeSvelteTestDataPath
import org.junit.Test

/**
 * Based on [com.intellij.lang.javascript.typescript.service.TypeScriptServiceDocumentationTest]
 */
class SvelteServiceDocumentationTest : SvelteServiceTestBase() {
  override fun getBasePath(): String = getRelativeSvelteTestDataPath() + "/dev/blachut/svelte/lang/service/documentation"

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    RegistryManager.getInstance().get("typescript.show.own.type").setValue(true, testRootDisposable)
  }

  @Test
  fun testNullChecks() = defaultQuickNavigateTest()

  @Test
  fun testReferenceUsedInsteadOfDeclaration() = defaultQuickNavigateTest()

  @Test
  fun testGenericType() = defaultQuickNavigateTest()

  @Test
  fun testQualifiedReference() = defaultQuickNavigateTest()

  private fun defaultQuickNavigateTest(directory: Boolean = false) {
    myFixture.configureByText("tsconfig.json", tsconfig)
    doDefaultHighlightingTest(directory)
    assertCorrectService()
    waitForDiagnosticsDataFromServer(project, file.virtualFile)

    val doc = JSAbstractDocumentationTest.getQuickNavigateText(myFixture)
    JSAbstractDocumentationTest.checkExpected(doc, testDataPath + "/" + getTestName(false) + ".expected.html")
  }

}

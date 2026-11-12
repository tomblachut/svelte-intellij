package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSDocumentationFixture
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationTargetProvider
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.getRelativeSvelteTestDataPath
import dev.blachut.svelte.lang.service.SvelteServiceFixture.Companion.svelteServiceFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @see com.intellij.lang.javascript.typescript.service.TypeScriptServiceDocumentationTest
 */
@TestApplication
class SvelteServiceDocumentationTest {
  private val svelte by svelteServiceFixture(getRelativeSvelteTestDataPath() + "/dev/blachut/svelte/lang/service/documentation")

  @BeforeEach
  fun setUp() {
    RegistryManager.getInstance().get("typescript.show.own.type").setValue(true, svelte.testRootDisposable)

    PsiDocumentationTargetProvider.EP_NAME.point
      .registerExtension(TypeScriptDocumentationTargetProvider(), svelte.testRootDisposable)

    JSLanguageServiceUtil.setTimeout(10_000, svelte.testRootDisposable)
  }

  @Test
  fun testNullChecks(): Unit = defaultComboTest()

  @Test
  fun testTypeNarrowing(): Unit = defaultComboTest()

  @Test
  fun testGenericType(): Unit = defaultComboTest()

  @Test
  fun testQualifiedReferenceToMethod(): Unit = defaultComboTest()

  @Test
  fun testQualifiedReferenceToProperty(): Unit = defaultComboTest()

  @Test
  fun testNoDummyResolve(): Unit = defaultComboTest()

  @Test
  fun testNoDummyResolve2(): Unit = defaultComboTest()

  @Test
  fun testKitDataProp(): Unit = defaultComboTest(true)

  private fun defaultComboTest(directory: Boolean = false) {
    svelte.configureDefault(directory)

    runInEdtAndWait { waitUntilFileOpenedByLspServer(svelte.project, svelte.fixture.file.virtualFile) }
    svelte.assertCorrectService()

    val quickNavigateText = JSDocumentationFixture.getQuickNavigateText(svelte.fixture)
    JSDocumentationFixture.checkExpected(quickNavigateText, "$testFileAbsolutePathWithoutExtension.nav.expected.html")

    if (directory) {
      val savedTestDataPath = svelte.fixture.testDataPath
      svelte.fixture.testDataPath = svelte.testDataPath + "/" + svelte.getTestName(false)
      try {
        runInEdtAndWait { svelte.fixture.checkDocumentationAtCaret() }
      }
      finally {
        svelte.fixture.testDataPath = savedTestDataPath
      }
    }
    else {
      val doc = timeoutRunBlocking { readActionBlocking { JSDocumentationFixture.getQuickDocumentationText(svelte.fixture) } } ?: "No documentation found."
      JSDocumentationFixture.checkExpected(doc, "$testFileAbsolutePathWithoutExtension.doc.expected.html")
    }
  }

  private val testFileAbsolutePathWithoutExtension
    get() = svelte.testDataPath + "/" + svelte.getTestName(false)
}

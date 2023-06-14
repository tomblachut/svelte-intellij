package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import junit.framework.TestCase

open class SvelteServiceTestBase : JSTempDirWithNodeInterpreterTest() {
  // compilerOptions taken from SvelteKit app
  protected val tsconfig = """
    {
      "compilerOptions": {
        "allowJs": true,
        "checkJs": true,
        "esModuleInterop": true,
        "forceConsistentCasingInFileNames": true,
        "resolveJsonModule": true,
        "skipLibCheck": true,
        "sourceMap": true,
        "strict": true
      }
    }
  """.trimIndent()

  override fun getExtension(): String = "svelte"

  override fun setUp() {
    super.setUp()

    val serviceSettings = getSvelteServiceSettings(project)
    val old = serviceSettings.serviceMode
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()

    Disposer.register(testRootDisposable) {
      serviceSettings.serviceMode = old
      TypeScriptLanguageServiceUtil.setUseService(false)
    }

    serviceSettings.serviceMode = SvelteServiceMode.ENABLED
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)

    SvelteLspExecutableDownloader.getExecutableOrRefresh(project) // could run blocking download
    assertNotNull(SvelteLspExecutableDownloader.getExecutable())
  }

  protected fun assertCorrectService() {
    val service = TypeScriptService.getForFile(project, file.virtualFile)
    UsefulTestCase.assertInstanceOf(service, SvelteLspTypeScriptService::class.java)
    TestCase.assertTrue(service!!.isServiceCreated())
  }

  protected fun doDefaultHighlightingTest(directory: Boolean) {
    if (directory) {
      doTestWithCopyDirectory()
    }
    else {
      defaultTest()
    }
  }
}
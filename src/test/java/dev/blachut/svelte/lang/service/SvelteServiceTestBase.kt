package dev.blachut.svelte.lang.service

import com.intellij.javascript.debugger.com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings

abstract class SvelteServiceTestBase : BaseLspTypeScriptServiceTest() {
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

    ensureServerDownloaded(SvelteLspExecutableDownloader)
  }

  protected fun assertCorrectService() {
    assertCorrectServiceImpl<SvelteLspTypeScriptService>()
  }
}
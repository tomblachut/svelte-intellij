package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import dev.blachut.svelte.lang.svelteKitPackageJson

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
      },
      // include is adapted from SvelteKit, but more loose
      "include": [
        "./**/*.js",
        "./**/*.ts",
        "./**/*.d.ts",
        "./**/*.svelte",
      ]
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

    myFixture.addFileToProject("package.json", svelteKitPackageJson)
  }

  protected fun assertCorrectService() {
    assertCorrectServiceImpl<SvelteLspTypeScriptService>()
  }

  protected fun assertCorrectServiceForTsFile() {
    assertCorrectServiceImpl<SveltePluginTypeScriptService>()
  }

  protected fun withTestDataPathOverriden(action: () -> Unit) {
    myFixture.testDataPath = getSvelteTestDataPath()
    try {
      action()
    }
    finally {
      myFixture.testDataPath = testDataPath
    }
  }

  protected fun configureDefault(directory: Boolean) {
    if (directory) {
      copyDirectory()
      withTestDataPathOverriden {
        myFixture.copyBundledSvelteKit()
      }
      myFixture.configureFromTempProjectFile("src/routes/+page.svelte")
    }
    else {
      myFixture.addFileToProject("tsconfig.json", tsconfig)
      myFixture.configureByFile(getTestName(false) + "." + extension)
    }
  }
}
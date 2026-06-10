package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptServiceDirectoryWatcher
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import dev.blachut.svelte.lang.svelteKitPackageJson

abstract class SvelteServiceTestBase : BaseLspTypeScriptServiceTest() {
  // compilerOptions taken from SvelteKit app
  private val tsconfig = """
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
    serviceSettings.serviceMode = SvelteServiceMode.DISABLED
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()

    Disposer.register(testRootDisposable) {
      serviceSettings.serviceMode = old
      TypeScriptLanguageServiceUtil.setUseService(false)
    }

    RegistryManager.getInstance().get("svelte.language.server.bundled.enabled").setValue(true, testRootDisposable)
    ensureServerDownloaded(SvelteLspServerLoader)

    myFixture.addFileToProject("package.json", svelteKitPackageJson)
    performNpmInstallForPackageJson("package.json") // svelte-language-server imports typescript
    prepareTypeScriptServiceDirectory()
    // npm installs project-local TypeScript; enable Svelte LSP only after that reload source is stable.
    serviceSettings.serviceMode = SvelteServiceMode.ENABLED
  }

  private fun prepareTypeScriptServiceDirectory() {
    val watcher = TypeScriptServiceDirectoryWatcher.getService(project)
    ReadAction.runBlocking<Exception> {
      watcher.calcServiceDirectoryAndRefresh()
    }
    runInEdtAndWait {
      watcher.update()
    }
  }

  protected fun addTypeScriptCommonFiles() {
    myFixture.addFileToProject("ambient.d.ts", """
      /// <reference types="svelte" />
      
      declare function __sveltets_2_invalidate<T>(getValue: () => T): T;
    """.trimIndent())
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
      addTypeScriptCommonFiles()
      myFixture.configureByFile(getTestName(false) + "." + extension)
    }
  }
}

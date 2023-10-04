package dev.blachut.svelte.lang.service

import com.intellij.javascript.debugger.com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath
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

  protected val packageJson = """
    {
    	"name": "svelte-test",
    	"version": "0.0.1",
    	"private": true,
    	"devDependencies": {
    		"@sveltejs/adapter-auto": "^2.0.0",
    		"@sveltejs/kit": "^1.20.4",
    		"svelte": "^4.0.5",
    		"svelte-check": "^3.4.3",
    		"tslib": "^2.4.1",
    		"typescript": "^5.0.0",
    		"vite": "^4.4.2"
    	},
    	"type": "module"
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

    myFixture.addFileToProject("package.json", packageJson)
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
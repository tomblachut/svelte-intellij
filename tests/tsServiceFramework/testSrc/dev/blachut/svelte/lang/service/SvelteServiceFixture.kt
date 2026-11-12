// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.service.LspTypeScriptServiceFixture
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import dev.blachut.svelte.lang.SVELTE_KIT_1_RUNTIME_DEPENDENCIES
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import dev.blachut.svelte.lang.svelteKitPackageJson
import org.junit.jupiter.api.Assertions.assertNotNull
import java.lang.reflect.Method

/**
 * The fixture of the Svelte language service tests.
 * Create it with [svelteServiceFixture].
 *
 * The setup turns on the TypeScript service and the bundled Svelte language server,
 * and adds the SvelteKit `package.json` and the Svelte 4 and SvelteKit 1 dependencies to the project.
 * The disposal of the test root disposable restores the Svelte service mode and turns off the TypeScript service.
 */
class SvelteServiceFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSTempDirFixtureOptions,
) : LspTypeScriptServiceFixture(fixture, testClass, testMethod, options) {

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
    RegistryManager.getInstance().get("typescript.service.completion.ownContributorsEnabled").setValue(false, testRootDisposable)
    assertNotNull(SvelteLspServerLoader.getAbsolutePath(project), "Bundled Svelte LSP server is not available")
    assertNotNull(SvelteTSPluginLoader.getAbsolutePath(project), "Bundled Svelte TypeScript plugin is not available")

    fixture.addFileToProject("package.json", svelteKitPackageJson)
    fixture.configureSvelteDependencies(
      SvelteTestModule.SVELTE_4,
      SvelteTestModule.SVELTE_KIT_1,
      SvelteTestModule.SVELTE_KIT_ADAPTER_AUTO_2,
      *SVELTE_KIT_1_RUNTIME_DEPENDENCIES,
      SvelteTestModule.SVELTE_PREPROCESS_5,
    )

    // Enable Svelte LSP only after fixture dependencies are copied and project roots are stable.
    serviceSettings.serviceMode = SvelteServiceMode.ENABLED
  }

  /** Adds an ambient declaration file and the `tsconfig.json` of a SvelteKit app to the project. */
  fun addTypeScriptCommonFiles() {
    fixture.addFileToProject("ambient.d.ts", """
      /// <reference types="svelte" />

      declare function __sveltets_2_invalidate<T>(getValue: () => T): T;
    """.trimIndent())
    fixture.addFileToProject("tsconfig.json", TSCONFIG)
  }

  /** Checks that the service of the current file is a started [SvelteLspTypeScriptService]. */
  fun assertCorrectService() {
    assertCorrectServiceImpl<SvelteLspTypeScriptService>()
  }

  /** Checks that the service of the current file is a started [SveltePluginTypeScriptService]. */
  fun assertCorrectServiceForTsFile() {
    assertCorrectServiceImpl<SveltePluginTypeScriptService>()
  }

  /** Runs [action] with the Svelte test data root as the test data path of [fixture]. */
  fun withTestDataPathOverriden(action: () -> Unit) {
    fixture.testDataPath = getSvelteTestDataPath()
    try {
      action()
    }
    finally {
      fixture.testDataPath = options.testDataPath
    }
  }

  /**
   * Configures the file of the test.
   * With [directory], it copies the test data directory and the bundled SvelteKit, and opens `src/routes/+page.svelte`.
   * Otherwise it adds the common TypeScript files and opens `<TestName>.svelte`.
   */
  fun configureDefault(directory: Boolean) {
    if (directory) {
      copyDirectory()
      withTestDataPathOverriden {
        fixture.copyBundledSvelteKit()
      }
      fixture.configureFromTempProjectFile("src/routes/+page.svelte")
    }
    else {
      addTypeScriptCommonFiles()
      fixture.configureByFile(getTestName(false) + "." + extension)
    }
  }

  companion object {
    // compilerOptions taken from SvelteKit app
    private const val TSCONFIG = """
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

    /**
     * Creates a [SvelteServiceFixture] on a new project, for the test data in [basePath] under the repository root.
     * Declare the result as an instance property, so that each test gets its own project.
     */
    fun svelteServiceFixture(basePath: String = ""): TestFixture<SvelteServiceFixture> =
      jsTempDirFixture("svelteServiceFixture", JSTempDirFixtureOptions.forBasePath(basePath).withExtension("svelte"), ::SvelteServiceFixture)
  }
}

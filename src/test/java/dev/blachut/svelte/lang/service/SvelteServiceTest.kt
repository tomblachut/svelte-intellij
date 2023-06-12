// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import junit.framework.TestCase
import org.junit.Test

class SvelteServiceTest : JSTempDirWithNodeInterpreterTest() {

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
    TestCase.assertNotNull(SvelteLspExecutableDownloader.getExecutable())

    myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
  fun testServiceWorks() {
    myFixture.configureByText("tsconfig.json", "{}")
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        let <error descr="Svelte: Type 'number' is not assignable to type 'string'.">local</error>: string = 1;
        local;
      </script>
      
      <input <warning descr="Svelte: A11y: Avoid using autofocus">autofocus</warning>>
    """)
    myFixture.doHighlighting()
    val service = TypeScriptService.getForFile(project, file.virtualFile)
    UsefulTestCase.assertInstanceOf(service, SvelteLspTypeScriptService::class.java)
    TestCase.assertTrue(service!!.isServiceCreated())
    myFixture.checkLspHighlighting()
  }
}
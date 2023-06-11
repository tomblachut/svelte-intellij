// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import junit.framework.TestCase
import org.junit.Test

class SvelteServiceTest : JSTempDirWithNodeInterpreterTest() {

  override fun setUp() {
    super.setUp()

    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()


    val oldRegistryValue = Registry.`is`("svelte.enable.lsp")
    Registry.get("svelte.enable.lsp").setValue(true)

    Disposer.register(testRootDisposable) {
      TypeScriptLanguageServiceUtil.setUseService(false)
      Registry.get("svelte.enable.lsp").setValue(oldRegistryValue)
    }

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
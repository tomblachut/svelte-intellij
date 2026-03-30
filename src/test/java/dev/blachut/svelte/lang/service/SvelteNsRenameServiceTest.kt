// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.junit.Test

/**
 * Tests for namespaced component rename functionality with LSP service.
 * Test data: resources/dev/blachut/svelte/lang/service/nsRename/
 */
class SvelteNsRenameServiceTest : SvelteServiceTestBase() {

  private val testDir: String get() = name.removePrefix("test").replaceFirstChar { it.lowercase() }

  private fun setupNsRenameProject(initFile: String = "Button.svelte") {
    addTypeScriptCommonFiles()
    withTestDataPathOverriden {
      myFixture.copyDirectoryToProject("dev/blachut/svelte/lang/service/nsRename/$testDir", "")
    }
    myFixture.configureFromTempProjectFile(initFile)
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  private fun checkNsRenameResult(vararg filesToCheck: String) {
    FileDocumentManager.getInstance().saveAllDocuments()
    val basePath = "dev/blachut/svelte/lang/service/nsRename/$testDir"
    withTestDataPathOverriden {
      for (file in filesToCheck) {
        val nameWithoutExt = file.substringBeforeLast('.')
        val ext = file.substringAfterLast('.')
        val dir = if (file.contains('/')) file.substringBeforeLast('/') + "/" else ""
        val name = if (file.contains('/')) file.substringAfterLast('/').substringBeforeLast('.') else nameWithoutExt
        myFixture.checkResultByFile(file, "$basePath/${dir}${name}_after.$ext", false)
      }
    }
  }

  @Test
  fun testFirstSegmentTwoLevel() {
    setupNsRenameProject()

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Components")

    checkNsRenameResult("Consumer.svelte")
  }

  @Test
  fun testNestedProperty() {
    setupNsRenameProject()

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureFromTempProjectFile("lib/UI.ts")
    val nestingBlock = myFixture.file.text.indexOf("Nesting:")
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Card,", nestingBlock))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    TestDialogManager.setTestDialog({ 1 }, testRootDisposable)
    myFixture.renameElementAtCaret("Panel")

    checkNsRenameResult("lib/UI.ts", "Consumer.svelte")
  }

  @Test
  fun testFromTemplateUsage() {
    setupNsRenameProject()

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    val tagText = "Nesting.Card"
    val tagOffset = myFixture.file.text.indexOf(tagText)
    myFixture.editor.caretModel.moveToOffset(tagOffset + "Nesting.".length)

    TestDialogManager.setTestDialog({ 1 }, testRootDisposable)
    myFixture.renameElementAtCaret("Panel")

    checkNsRenameResult("lib/UI.ts", "Consumer.svelte")
  }

  @Test
  fun testFirstSegmentThreeLevel() {
    setupNsRenameProject("Label.svelte")

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Widgets")

    checkNsRenameResult("Consumer.svelte")
  }
}

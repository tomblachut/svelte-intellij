// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.service.SvelteServiceFixture.Companion.svelteServiceFixture
import org.junit.jupiter.api.Test

/**
 * Tests for namespaced component rename functionality with LSP service.
 * Test data: resources/dev/blachut/svelte/lang/service/nsRename/
 */
@TestApplication
class SvelteNsRenameServiceTest {
  private val svelte by svelteServiceFixture()

  private val testDir: String get() = svelte.testMethod.name.removePrefix("test").replaceFirstChar { it.lowercase() }

  private fun setupNsRenameProject(initFile: String = "Button.svelte") {
    svelte.addTypeScriptCommonFiles()
    svelte.withTestDataPathOverriden {
      svelte.fixture.copyDirectoryToProject("dev/blachut/svelte/lang/service/nsRename/$testDir", "")
    }
    svelte.fixture.configureFromTempProjectFile(initFile)
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  private fun checkNsRenameResult(vararg filesToCheck: String) {
    runInEdtAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
    val basePath = "dev/blachut/svelte/lang/service/nsRename/$testDir"
    svelte.withTestDataPathOverriden {
      for (file in filesToCheck) {
        val nameWithoutExt = file.substringBeforeLast('.')
        val ext = file.substringAfterLast('.')
        val dir = if (file.contains('/')) file.substringBeforeLast('/') + "/" else ""
        val name = if (file.contains('/')) file.substringAfterLast('/').substringBeforeLast('.') else nameWithoutExt
        svelte.fixture.checkResultByFile(file, "$basePath/${dir}${name}_after.$ext", false)
      }
    }
  }

  @Test
  fun testFirstSegmentTwoLevel() {
    setupNsRenameProject()

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    runInEdtAndWait {
      svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("UI from"))
      svelte.fixture.renameElementAtCaret("Components")
    }

    checkNsRenameResult("Consumer.svelte")
  }

  @Test
  fun testNestedProperty() {
    setupNsRenameProject()

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("lib/UI.ts")
    runInEdtAndWait {
      val nestingBlock = svelte.fixture.file.text.indexOf("Nesting:")
      svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("Card,", nestingBlock))
    }
    svelte.fixture.doHighlighting()
    svelte.assertCorrectServiceForTsFile()

    TestDialogManager.setTestDialog({ 1 }, svelte.testRootDisposable)
    runInEdtAndWait { svelte.fixture.renameElementAtCaret("Panel") }

    checkNsRenameResult("lib/UI.ts", "Consumer.svelte")
  }

  @Test
  fun testFromTemplateUsage() {
    setupNsRenameProject()

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    val tagText = "Nesting.Card"
    runInEdtAndWait {
      val tagOffset = svelte.fixture.file.text.indexOf(tagText)
      svelte.fixture.editor.caretModel.moveToOffset(tagOffset + "Nesting.".length)
    }

    TestDialogManager.setTestDialog({ 1 }, svelte.testRootDisposable)
    runInEdtAndWait { svelte.fixture.renameElementAtCaret("Panel") }

    checkNsRenameResult("lib/UI.ts", "Consumer.svelte")
  }

  @Test
  fun testFirstSegmentThreeLevel() {
    setupNsRenameProject("Label.svelte")

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    runInEdtAndWait {
      svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("UI from"))
      svelte.fixture.renameElementAtCaret("Widgets")
    }

    checkNsRenameResult("Consumer.svelte")
  }
}

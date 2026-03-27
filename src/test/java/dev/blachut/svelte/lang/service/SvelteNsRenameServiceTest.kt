// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import junit.framework.TestCase
import org.junit.Test

/**
 * Tests for namespaced component rename functionality with LSP service.
 */
class SvelteNsRenameServiceTest : SvelteServiceTestBase() {

  private fun setupTwoLevelForRename() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      export default { Button };
    """.trimIndent())

    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import UI from './lib/UI';
      </script>
      <UI.Button>Click me</UI.Button>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  private fun setupThreeLevelForRename() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/Button.ts", """
      import Label from '../Label.svelte';
      export default { Label };
    """.trimIndent())
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from './Button';
      export default { Button };
    """.trimIndent())

    myFixture.configureByText("Label.svelte", "<span><slot /></span>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import UI from './lib/UI';
      </script>
      <UI.Button.Label>Submit</UI.Button.Label>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testFirstSegmentTwoLevel() {
    setupTwoLevelForRename()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Components")

    val text = myFixture.file.text
    TestCase.assertTrue("Expected <Components.Button>, got:\n$text", text.contains("<Components.Button>"))
    TestCase.assertTrue("Expected </Components.Button>, got:\n$text", text.contains("</Components.Button>"))
    TestCase.assertFalse("'Button' segment must not change, got:\n$text", text.contains("<Components.Components>"))
  }

  @Test
  fun testNestedProperty() {
    // Rename "Card" inside Nesting: { Button, Card } from <Components.Nesting.Card>
    // should rename the nested property and its template usages,
    // but NOT touch <Components.Button> (top-level sibling) or <Button> (direct import)
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';
      export default {
        Nesting: {
          Button,
          Card,
        },
        Button,
      };
    """.trimIndent())

    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import Components from './lib/UI';
        import Button from './Button.svelte';
      </script>
      <Components.Nesting.Card />
      <Components.Nesting.Button />
      <Components.Button />
      <Button />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureFromTempProjectFile("lib/UI.ts")
    val nestingBlock = myFixture.file.text.indexOf("Nesting:")
    val nestedCard = myFixture.file.text.indexOf("Card,", nestingBlock)
    myFixture.editor.caretModel.moveToOffset(nestedCard)
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    TestDialogManager.setTestDialog({ 1 }, testRootDisposable)
    myFixture.renameElementAtCaret("Panel")

    val uiText = myFixture.file.text
    assertTrue("Expected 'Panel' in Nesting block, got:\n$uiText",
      uiText.contains("Panel"))
    assertTrue("Expected 'Button' still present, got:\n$uiText",
      uiText.contains("Button"))

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    val consumerText = myFixture.file.text
    assertTrue("Expected <Components.Nesting.Panel>, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Panel"))
    assertTrue("Expected <Components.Nesting.Button> unchanged, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Button"))
    assertTrue("Expected <Components.Button> unchanged, got:\n$consumerText",
      consumerText.contains("<Components.Button"))
    assertTrue("Expected <Button> direct import unchanged, got:\n$consumerText",
      consumerText.contains("<Button />"))
  }

  @Test
  fun testFromTemplateUsage() {
    // Rename "Card" from template usage <Components.Nesting.Card> → <Components.Nesting.Panel>
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';
      export default {
        Nesting: {
          Button,
          Card,
        },
      };
    """.trimIndent())

    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import Components from './lib/UI';
      </script>
      <Components.Nesting.Card />
      <Components.Nesting.Button />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    val tagText = "Nesting.Card"
    val tagOffset = myFixture.file.text.indexOf(tagText)
    myFixture.editor.caretModel.moveToOffset(tagOffset + "Nesting.".length)

    TestDialogManager.setTestDialog({ 1 }, testRootDisposable)
    myFixture.renameElementAtCaret("Panel")

    val consumerText = myFixture.file.text
    assertTrue("Expected <Components.Nesting.Panel>, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Panel"))
    assertTrue("Expected <Components.Nesting.Button> unchanged, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Button"))

    FileDocumentManager.getInstance().saveAllDocuments()
    myFixture.configureFromTempProjectFile("lib/UI.ts")
    val uiText = myFixture.file.text
    assertTrue("Expected 'Panel' in Nesting block, got:\n$uiText",
      uiText.contains("Panel"))
  }

  @Test
  fun testFirstSegmentThreeLevel() {
    // Rename "UI" from import → <UI.Button.Label> → <Widgets.Button.Label>
    setupThreeLevelForRename()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Widgets")

    val text = myFixture.file.text
    TestCase.assertTrue("Expected <Widgets.Button.Label>, got:\n$text", text.contains("<Widgets.Button.Label>"))
    TestCase.assertTrue("Expected </Widgets.Button.Label>, got:\n$text", text.contains("</Widgets.Button.Label>"))
  }
}

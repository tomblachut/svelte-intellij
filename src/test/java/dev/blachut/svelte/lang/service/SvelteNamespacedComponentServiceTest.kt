// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.testFramework.web.fileUsages
import com.intellij.javascript.testFramework.web.usagesAtCaret
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import junit.framework.TestCase
import org.junit.Test

/**
 * Tests for namespaced component find usages functionality with LSP service.
 * These tests verify that "Show Component Usages" works correctly for components
 * used via namespace imports like `<UI.Button>`.
 */
class SvelteNamespacedComponentServiceTest : SvelteServiceTestBase() {

  @Test
  fun testUsagesImportNotUnusedWithLsp() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';
      export default { Button, Card };
    """.trimIndent())

    // First open a Svelte file with checkLspHighlighting to initialize services
    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import UI from './lib/UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testUsagesFindWithLsp() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/index.ts", """
      export { default as Button } from '../Button.svelte';
      export { default as Card } from '../Card.svelte';
    """.trimIndent())

    // First open a Svelte file with checkLspHighlighting to initialize services
    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import * as UI from './lib/index';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Switch to Button.svelte to test "Show Component Usages" (fileUsages combines ReferencesSearch + CustomUsageSearcher)
    myFixture.configureFromTempProjectFile("Button.svelte")
    val usages = myFixture.fileUsages()
    TestCase.assertTrue(
      "Expected re-export usage in index.ts, got: $usages",
      usages.any { it.contains("index.ts") }
    )
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testUsagesWithDefaultObjectExport() {
    // Tests the pattern where components are re-exported via a default object:
    // export default { Button, Card }
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())

    // First open a Svelte file with checkLspHighlighting to initialize services
    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import UI from './lib/UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Switch to Card.svelte to test "Show Component Usages"
    myFixture.configureFromTempProjectFile("Card.svelte")
    val usages = myFixture.fileUsages()
    TestCase.assertTrue(
      "Expected import usage in UI.ts, got: $usages",
      usages.any { it.contains("UI.ts") }
    )
    TestCase.assertTrue(
      "Expected template usage <UI.Card> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testNsUsagesFromImport() {
    // Tests find usages when caret is on the import binding in the barrel file:
    // import But<caret>ton from './Button.svelte';
    // This finds usages of the local variable, which includes the shorthand property in the export
    // and template usages in Svelte files (via SvelteReferencesSearch).
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("UI.ts", """
      import Button from './Button.svelte';
      import Card from './Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())
    myFixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import UI from './UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())

    // First open a Svelte file with checkLspHighlighting to initialize services
    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    // Open Consumer.svelte to register it with LSP
    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Now configure the TS file for find usages
    myFixture.configureFromTempProjectFile("UI.ts")
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Button from"))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    val usages = myFixture.usagesAtCaret()
    // Find usages from the import binding finds the shorthand property usage in the same file
    TestCase.assertTrue(
      "Expected export usage in UI.ts, got: $usages",
      usages.any { it.contains("UI.ts") && it.contains("Button") }
    )
    // Also finds template usages in Svelte files via SvelteReferencesSearch
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testDeepNsUsagesFromFile() {
    // Tests 3-level namespaced component: <Forms.Button.Label>
    // Forms.ts → Button.ts → Label.svelte
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/Button.ts", """
      import Label from '../Label.svelte';
      import Icon from '../Icon.svelte';
      export default { Label, Icon };
    """.trimIndent())
    myFixture.addFileToProject("lib/Forms.ts", """
      import Button from './Button';
      export default { Button };
    """.trimIndent())

    myFixture.configureByText("Label.svelte", "<span><slot /></span>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Icon.svelte", "<svg><slot /></svg>")

    myFixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import Forms from './lib/Forms';
      </script>
      <Forms.Button.Label>Submit</Forms.Button.Label>
      <Forms.Button.Icon />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Find usages from the Label.svelte file
    myFixture.configureFromTempProjectFile("Label.svelte")
    val usages = myFixture.fileUsages()
    TestCase.assertTrue(
      "Expected import usage in Button.ts, got: $usages",
      usages.any { it.contains("Button.ts") }
    )
    TestCase.assertTrue(
      "Expected template usage <Forms.Button.Label> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testDeepNsUsagesFromImport() {
    // Tests Find Usages from import binding in 3-level chain:
    // import La<caret>bel from '../Label.svelte' in Button.ts
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/Button.ts", """
      import Label from '../Label.svelte';
      import Icon from '../Icon.svelte';
      export default { Label, Icon };
    """.trimIndent())
    myFixture.addFileToProject("lib/Forms.ts", """
      import Button from './Button';
      export default { Button };
    """.trimIndent())
    myFixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import Forms from './lib/Forms';
      </script>
      <Forms.Button.Label>Submit</Forms.Button.Label>
    """.trimIndent())

    myFixture.configureByText("Label.svelte", "<span><slot /></span>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Icon.svelte", "<svg><slot /></svg>")

    // Open Consumer.svelte to register it with LSP
    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Find usages from the import binding in Button.ts
    myFixture.configureFromTempProjectFile("lib/Button.ts")
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Label from"))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    val usages = myFixture.usagesAtCaret()
    TestCase.assertTrue(
      "Expected export usage in Button.ts, got: $usages",
      usages.any { it.contains("Button.ts") && it.contains("Label") }
    )
    TestCase.assertTrue(
      "Expected template usage <Forms.Button.Label> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testNsUsagesFromExport() {
    // Tests find usages when caret is on the shorthand property in the export:
    // export default { But<caret>ton, Card };
    // This finds usages of the local variable reference, which resolves back to the import
    // and template usages in Svelte files (via SvelteReferencesSearch).
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("UI.ts", """
      import Button from './Button.svelte';
      import Card from './Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())
    myFixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import UI from './UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())

    // First open a Svelte file with checkLspHighlighting to initialize services
    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    // Open Consumer.svelte to register it with LSP
    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Now configure the TS file for find usages
    myFixture.configureFromTempProjectFile("UI.ts")
    // Position caret on Button in "export default { Button,"
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Button,"))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    val usages = myFixture.usagesAtCaret()
    // Find usages from the shorthand property finds the import declaration
    TestCase.assertTrue(
      "Expected import usage in UI.ts, got: $usages",
      usages.any { it.contains("UI.ts") }
    )
    // Also finds template usages in Svelte files via SvelteReferencesSearch
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

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

  @Test
  fun testRenameFirstSegmentInTwoLevel() {
    // Rename "UI" from import → <UI.Button> → <Components.Button>, "Button" must NOT change
    setupTwoLevelForRename()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Components")

    val text = myFixture.file.text
    TestCase.assertTrue("Expected <Components.Button>, got:\n$text", text.contains("<Components.Button>"))
    TestCase.assertTrue("Expected </Components.Button>, got:\n$text", text.contains("</Components.Button>"))
    TestCase.assertFalse("'Button' segment must not change, got:\n$text", text.contains("<Components.Components>"))
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
  fun testRenameInNestedNs() {
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
      <Components.Nesting.Button prop="nested" />
      <Components.Button prop="top level" />
      <Button prop="direct import" />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    // Place caret on "Card," in the Nesting property of UI.ts — a unique shorthand property
    myFixture.configureFromTempProjectFile("lib/UI.ts")
    val nestingBlock = myFixture.file.text.indexOf("Nesting:")
    val nestedCard = myFixture.file.text.indexOf("Card,", nestingBlock)
    myFixture.editor.caretModel.moveToOffset(nestedCard)
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    // Select PROPERTY (index 1) in the shorthand property rename dialog
    // to rename only the property key, not the imported variable
    TestDialogManager.setTestDialog({ 1 }, testRootDisposable)
    myFixture.renameElementAtCaret("Panel")

    // Check UI.ts: nested Card renamed to Panel, other properties unchanged
    val uiText = myFixture.file.text
    assertTrue("Expected 'Panel' in Nesting block, got:\n$uiText",
      uiText.contains("Panel"))
    assertTrue("Expected 'Button' still present, got:\n$uiText",
      uiText.contains("Button"))

    // Check Consumer.svelte
    myFixture.configureFromTempProjectFile("Consumer.svelte")
    val consumerText = myFixture.file.text
    assertTrue("Expected <Components.Nesting.Panel>, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Panel"))
    assertTrue("Expected <Components.Nesting.Button> unchanged, got:\n$consumerText",
      consumerText.contains("<Components.Nesting.Button"))
    assertTrue("Expected <Components.Button> unchanged, got:\n$consumerText",
      consumerText.contains("<Components.Button"))
    assertTrue("Expected <Button> direct import unchanged, got:\n$consumerText",
      consumerText.contains("<Button prop=\"direct import\""))
  }

  @Test
  fun testRenameFirstSegmentInThreeLevel() {
    // Rename "UI" from import → <UI.Button.Label> → <Widgets.Button.Label>
    // "Button" and "Label" must NOT change
    setupThreeLevelForRename()

    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("UI from"))
    myFixture.renameElementAtCaret("Widgets")

    val text = myFixture.file.text
    TestCase.assertTrue("Expected <Widgets.Button.Label>, got:\n$text", text.contains("<Widgets.Button.Label>"))
    TestCase.assertTrue("Expected </Widgets.Button.Label>, got:\n$text", text.contains("</Widgets.Button.Label>"))
  }

}

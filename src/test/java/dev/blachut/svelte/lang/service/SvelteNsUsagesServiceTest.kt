// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.testFramework.web.fileUsages
import com.intellij.javascript.testFramework.web.usagesAtCaret
import com.intellij.platform.lsp.tests.checkLspHighlighting
import junit.framework.TestCase
import org.junit.Test

/**
 * Tests for namespaced component find usages functionality with LSP service.
 * These tests verify that "Show Component Usages" works correctly for components
 * used via namespace imports like `<UI.Button>`.
 */
class SvelteNsUsagesServiceTest : SvelteServiceTestBase() {
  @Test
  fun testDefaultObjectExport() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())

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
  fun testFromImport() {
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

    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureFromTempProjectFile("UI.ts")
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Button from"))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    val usages = myFixture.usagesAtCaret()
    TestCase.assertTrue(
      "Expected export usage in UI.ts, got: $usages",
      usages.any { it.contains("UI.ts") && it.contains("Button") }
    )
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }

  @Test
  fun testDeepFromFile() {
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
  fun testDeepFromImport() {
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

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

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
  fun testFromExport() {
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

    myFixture.configureByText("Button.svelte", "<button><slot /></button>")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    myFixture.configureFromTempProjectFile("Consumer.svelte")
    myFixture.checkLspHighlighting()
    assertCorrectService()

    myFixture.configureFromTempProjectFile("UI.ts")
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("Button,"))
    myFixture.doHighlighting()
    assertCorrectServiceForTsFile()

    val usages = myFixture.usagesAtCaret()
    TestCase.assertTrue(
      "Expected import usage in UI.ts, got: $usages",
      usages.any { it.contains("UI.ts") }
    )
    TestCase.assertTrue(
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages",
      usages.any { it.contains("Consumer.svelte") }
    )
  }
}

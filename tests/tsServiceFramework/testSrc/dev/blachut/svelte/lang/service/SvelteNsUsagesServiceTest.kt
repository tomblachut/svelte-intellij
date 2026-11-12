// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.polySymbols.testFramework.fileUsages
import com.intellij.polySymbols.testFramework.usagesAtCaret
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.service.SvelteServiceFixture.Companion.svelteServiceFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for namespaced component find usages functionality with LSP service.
 * These tests verify that "Show Component Usages" works correctly for components
 * used via namespace imports like `<UI.Button>`.
 */
@TestApplication
class SvelteNsUsagesServiceTest {
  private val svelte by svelteServiceFixture()

  @Test
  fun testDefaultObjectExport() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("lib/UI.ts", """
      import Button from '../Button.svelte';
      import Card from '../Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())

    svelte.fixture.configureByText("Button.svelte", "<button><slot /></button>")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    svelte.fixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import UI from './lib/UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("Card.svelte")
    val usages = runReadActionBlocking { svelte.fixture.fileUsages() }
    assertTrue(
      usages.any { it.contains("UI.ts") },
      "Expected import usage in UI.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <UI.Card> in Consumer.svelte, got: $usages"
    )
  }

  @Test
  fun testFromImport() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("UI.ts", """
      import Button from './Button.svelte';
      import Card from './Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())
    svelte.fixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import UI from './UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())

    svelte.fixture.configureByText("Button.svelte", "<button><slot /></button>")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("UI.ts")
    runInEdtAndWait { svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("Button from")) }
    svelte.fixture.doHighlighting()
    svelte.assertCorrectServiceForTsFile()

    val usages = runReadActionBlocking { svelte.fixture.usagesAtCaret() }
    assertTrue(
      usages.any { it.contains("UI.ts") && it.contains("Button") },
      "Expected export usage in UI.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages"
    )
  }

  @Test
  fun testDeepFromFile() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("lib/Button.ts", """
      import Label from '../Label.svelte';
      import Icon from '../Icon.svelte';
      export default { Label, Icon };
    """.trimIndent())
    svelte.fixture.addFileToProject("lib/Forms.ts", """
      import Button from './Button';
      export default { Button };
    """.trimIndent())

    svelte.fixture.configureByText("Label.svelte", "<span><slot /></span>")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Icon.svelte", "<svg><slot /></svg>")

    svelte.fixture.configureByText("Consumer.svelte", """
      <script lang="ts">
        import Forms from './lib/Forms';
      </script>
      <Forms.Button.Label>Submit</Forms.Button.Label>
      <Forms.Button.Icon />
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("Label.svelte")
    val usages = runReadActionBlocking { svelte.fixture.fileUsages() }
    assertTrue(
      usages.any { it.contains("Button.ts") },
      "Expected import usage in Button.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <Forms.Button.Label> in Consumer.svelte, got: $usages"
    )
  }

  @Test
  fun testDeepFromImport() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("lib/Button.ts", """
      import Label from '../Label.svelte';
      import Icon from '../Icon.svelte';
      export default { Label, Icon };
    """.trimIndent())
    svelte.fixture.addFileToProject("lib/Forms.ts", """
      import Button from './Button';
      export default { Button };
    """.trimIndent())
    svelte.fixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import Forms from './lib/Forms';
      </script>
      <Forms.Button.Label>Submit</Forms.Button.Label>
    """.trimIndent())

    svelte.fixture.configureByText("Label.svelte", "<span><slot /></span>")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Icon.svelte", "<svg><slot /></svg>")

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("lib/Button.ts")
    runInEdtAndWait { svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("Label from")) }
    svelte.fixture.doHighlighting()
    svelte.assertCorrectServiceForTsFile()

    val usages = runReadActionBlocking { svelte.fixture.usagesAtCaret() }
    assertTrue(
      usages.any { it.contains("Button.ts") && it.contains("Label") },
      "Expected export usage in Button.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <Forms.Button.Label> in Consumer.svelte, got: $usages"
    )
  }

  @Test
  fun testReExportFromFile() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("index.ts", """
      export { default as Button } from "./Button.svelte";
    """.trimIndent())

    svelte.fixture.configureByText("Button.svelte", """
      <script>
        export let label = "";
      </script>
      <button>{label}</button>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureByText("Example.svelte", """
      <script lang="ts">
        import * as UI from "./index";
      </script>
      <UI.Button label="Click me" />
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("Button.svelte")
    val usages = runReadActionBlocking { svelte.fixture.fileUsages() }
    assertTrue(
      usages.any { it.contains("index.ts") },
      "Expected usage in index.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Example.svelte") },
      "Expected template usage <UI.Button> in Example.svelte, got: $usages"
    )
  }

  @Test
  fun testNamespaceReExportFromFile() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("lib/index.ts", """
      export { default as Button } from '../Button.svelte';
      export { default as Card } from '../Card.svelte';
    """.trimIndent())

    svelte.fixture.configureByText("Button.svelte", """
      <script>
        export let variant = "default";
      </script>
      <button class={variant}><slot /></button>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    svelte.fixture.configureByText("Consumer.svelte", """
      <script>
        import * as UI from './lib/index';
      </script>
      <UI.Card>
        <UI.Button variant="primary">Click me</UI.Button>
      </UI.Card>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("Button.svelte")
    val usages = runReadActionBlocking { svelte.fixture.fileUsages() }
    assertTrue(
      usages.any { it.contains("index.ts") },
      "Expected re-export usage in index.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages"
    )
  }

  @Test
  fun testFromExport() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("UI.ts", """
      import Button from './Button.svelte';
      import Card from './Card.svelte';

      export default {
        Button,
        Card
      };
    """.trimIndent())
    svelte.fixture.addFileToProject("Consumer.svelte", """
      <script lang="ts">
        import UI from './UI';
      </script>
      <UI.Card>
        <UI.Button>Click me</UI.Button>
      </UI.Card>
    """.trimIndent())

    svelte.fixture.configureByText("Button.svelte", "<button><slot /></button>")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.addFileToProject("Card.svelte", "<div class=\"card\"><slot /></div>")

    svelte.fixture.configureFromTempProjectFile("Consumer.svelte")
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    svelte.fixture.configureFromTempProjectFile("UI.ts")
    runInEdtAndWait { svelte.fixture.editor.caretModel.moveToOffset(svelte.fixture.file.text.indexOf("Button,")) }
    svelte.fixture.doHighlighting()
    svelte.assertCorrectServiceForTsFile()

    val usages = runReadActionBlocking { svelte.fixture.usagesAtCaret() }
    assertTrue(
      usages.any { it.contains("UI.ts") },
      "Expected import usage in UI.ts, got: $usages"
    )
    assertTrue(
      usages.any { it.contains("Consumer.svelte") },
      "Expected template usage <UI.Button> in Consumer.svelte, got: $usages"
    )
  }
}

package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

@TestApplication
class SvelteNavigationTest {
    companion object {
        private val projectFixture = svelteProjectFixture()
    }

    private val myFixture by svelteCodeInsightFixture(projectFixture)

    @Test
    fun testComponentNameJS() {
        myFixture.configureWidgetFile()
        myFixture.configureByText("Example.svelte", """
            <script>
                import Widget from "./Widget.svelte";
            </script>

            <Wid<caret>get />
        """.trimIndent())

        val prevCaretOffset = runReadActionBlocking { myFixture.caretOffset }
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // If navigation goes to different file, caret in initiating editor stays in place, otherwise it’s moved to declaration and the test fails.
        assertEquals(prevCaretOffset, runReadActionBlocking { myFixture.caretOffset })
    }

    @Test
    fun testComponentNameTS() {
        myFixture.configureWidgetFile()
        myFixture.configureByText("Example.svelte", """
            <script lang="ts">
                import Widget from "./Widget.svelte";
            </script>

            <Wid<caret>get />
        """.trimIndent())

        val prevCaretOffset = runReadActionBlocking { myFixture.caretOffset }
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // If navigation goes to different file, caret in initiating editor stays in place, otherwise it’s moved to declaration and the test fails.
        assertEquals(prevCaretOffset, runReadActionBlocking { myFixture.caretOffset })

        // can be combined with to test e.g. component props JSTestUtils.getGotoDeclarationTarget(myFixture, expectedTargetFile)
    }

    @Test
    fun testNamespacedComponentNamespaceNavigation() {
        myFixture.configureByText("Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <For<caret>ms.Input />
        """.trimIndent())

        val prevCaretOffset = runReadActionBlocking { myFixture.caretOffset }
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // Navigation goes to the Forms import binding in the same file, so caret should move.
        assertNotSame(prevCaretOffset, runReadActionBlocking { myFixture.caretOffset })
    }

    private fun CodeInsightTestFixture.configureWidgetFile(): PsiFile {
        return configureByText("Widget.svelte", """
            <script>
                export let aProp = "";
            </script>
        """.trimIndent())
    }
}

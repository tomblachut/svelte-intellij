package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import junit.framework.TestCase

class SvelteNavigationTest : BasePlatformTestCase() {
    fun testComponentNameJS() {
        myFixture.configureWidgetFile()
        myFixture.configureByText("Example.svelte", """
            <script>
                import Widget from "./Widget.svelte";
            </script>

            <Wid<caret>get />
        """.trimIndent())

        val prevCaretOffset = myFixture.caretOffset
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // If navigation goes to different file, caret in initiating editor stays in place, otherwise it’s moved to declaration and the test fails.
        TestCase.assertEquals(prevCaretOffset, myFixture.caretOffset)
    }

    fun testComponentNameTS() {
        myFixture.configureWidgetFile()
        myFixture.configureByText("Example.svelte", """
            <script lang="ts">
                import Widget from "./Widget.svelte";
            </script>

            <Wid<caret>get />
        """.trimIndent())

        val prevCaretOffset = myFixture.caretOffset
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // If navigation goes to different file, caret in initiating editor stays in place, otherwise it’s moved to declaration and the test fails.
        TestCase.assertEquals(prevCaretOffset, myFixture.caretOffset)

        // can be combined with to test e.g. component props JSTestUtils.getGotoDeclarationTarget(myFixture, expectedTargetFile)
    }

    fun testNamespacedComponentNamespaceNavigation() {
        myFixture.configureByText("Example.svelte", """
            <script>
                import * as Forms from "./forms";
            </script>

            <For<caret>ms.Input />
        """.trimIndent())

        val prevCaretOffset = myFixture.caretOffset
        myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
        // Navigation goes to the Forms import binding in the same file, so caret should move.
        TestCase.assertNotSame(prevCaretOffset, myFixture.caretOffset)
    }

    private fun CodeInsightTestFixture.configureWidgetFile(): PsiFile {
        return configureByText("Widget.svelte", """
            <script>
                export let aProp = "";
            </script>
        """.trimIndent())
    }
}

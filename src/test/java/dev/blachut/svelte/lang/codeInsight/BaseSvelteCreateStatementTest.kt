package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.junit.jupiter.api.Test

abstract class BaseSvelteCreateStatementTest(
    projectFixture: TestFixture<Project>,
    private val requestedAction: String,
    private val createdStatement: String,
    private val afterCaret: String,
    private val optionalLine: String = "",
) : AbstractSvelteCreateStatementTest(projectFixture) {
    protected val caret = "<caret>"
    protected val basicResult // getter so that indentation is aligned
        get() = """
            <script>
$createdStatement
            </script>

            {unresolved$afterCaret}
        """.trimIndent()


    @Test
    override fun testScriptMissing() {
        doTest("""
            {unresolved$caret$afterCaret}
        """.trimIndent(), basicResult)
    }

    @Test
    override fun testScriptCollapsed() {
        doTest(
            """
            <script />

            {unresolved$caret$afterCaret}
        """.trimIndent(), basicResult)
    }

    @Test
    override fun testScriptEmpty() {
        doTest("""
            <script></script>

            {unresolved$caret$afterCaret}
        """.trimIndent(), basicResult)
    }

    @Test
    override fun testScriptBlank() {
        doTest("""
            <script>
            </script>

            {unresolved$caret$afterCaret}
        """.trimIndent(), basicResult)
    }

    @Test
    override fun testScriptNonEmpty() {
        doTest("""
            <script>
                let existingVariable = 5;
            </script>

            {unresolved$caret$afterCaret}
        """.trimIndent(), """
            <script>
                let existingVariable = 5;
$optionalLine$createdStatement
            </script>

            {unresolved$afterCaret}
        """.trimIndent()
        )
    }

    @Test
    override fun testNonConventionalScriptOrder() {
        doTest("""
            {unresolved$caret$afterCaret}

            <script>
                let existingVariable = 5;
            </script>
        """.trimIndent(), """
            {unresolved$afterCaret}

            <script>
                let existingVariable = 5;
$optionalLine$createdStatement
            </script>
        """.trimIndent()
        )
    }

    @Test
    override fun testInsideScriptStillWorksJS() {
        doTest("""
            <script>
                let existingVariable = 5;

                unresolved$caret$afterCaret
            </script>
        """.trimIndent(), """
            <script>
                let existingVariable = 5;

$createdStatement$optionalLine
                unresolved$afterCaret
            </script>
        """.trimIndent()
        )
    }

    @Test
    override fun testInsideScriptStillWorksTS() {
        doTest("""
            <script lang="ts">
                let existingVariable = 5;

                unresolved$caret$afterCaret
            </script>
        """.trimIndent(), """
            <script lang="ts">
                let existingVariable = 5;

$createdStatement$optionalLine
                unresolved$afterCaret
            </script>
        """.trimIndent()
        )
    }

    @Test
    override fun testScriptMissingWithAdjacentModuleScript() {
        doTest("""
            <script module>
                let existingVariable = 5;
            </script>

            {unresolved$caret$afterCaret}
        """.trimIndent(), """
            <script module>
                let existingVariable = 5;
            </script>
            <script>
$createdStatement
            </script>

            {unresolved$afterCaret}
        """.trimIndent()
        )
    }

    @Test
    override fun testInsideModuleScriptWithAdjacentInstanceScript() {
        doTest("""
            <script context="module">
                unresolved$caret$afterCaret
            </script>
            <script>
                let existingVariable = 5;
            </script>
        """.trimIndent(), """
            <script context="module">
$createdStatement$optionalLine
                unresolved$afterCaret
            </script>
            <script>
                let existingVariable = 5;
            </script>
        """.trimIndent()
        )
    }

    private fun doTest(before: String, after: String) {
        myFixture.configureByText("Example.svelte", before)

        myFixture.launchAction(myFixture.findSingleIntention(requestedAction))
        myFixture.checkResult(after)
    }

    companion object {
        internal const val replacedIndent = "                "
    }
}

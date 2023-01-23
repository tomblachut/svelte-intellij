package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection
import dev.blachut.svelte.lang.inspections.SvelteUnresolvedComponentInspection

class SvelteCreateImportTest : AbstractSvelteCreateStatementTest() {
    private val requestedActionHint: String = """Insert 'import"""
    private val createdStatement: String = """import {exported} from "./utils";"""

    private val caret = "<caret>"

    override fun testScriptMissing() {
        doTest(
            """
                {exported$caret}
            """.trimIndent(),
            """
                <script>
                    $createdStatement
                </script>

                {exported}
            """.trimIndent()
        )
    }

    override fun testScriptCollapsed() {
        doTest(
            """
                <script />

                {exported$caret}
            """.trimIndent(),
            """
                <script>
                $createdStatement
                </script>

                {exported}
            """.trimIndent()
        )
    }

    override fun testScriptEmpty() {
        doTest(
            """
                <script></script>

                {exported$caret}
            """.trimIndent(),
            """
                <script>
                $createdStatement
                </script>

                {exported}
            """.trimIndent()
        )
    }

    override fun testScriptBlank() {
        doTest(
            """
                <script>
                </script>

                {exported$caret}
            """.trimIndent(),
            """
                <script>
                $createdStatement
                </script>

                {exported}
            """.trimIndent())
    }

    override fun testScriptNonEmpty() {
        doTest(
            """
                <script>
                    let existingVariable = 5;
                </script>

                {exported$caret}
            """.trimIndent(),
            """
                <script>
                    $createdStatement

                    let existingVariable = 5;
                </script>

                {exported}
            """.trimIndent()
        )
    }

    override fun testNonConventionalScriptOrder() {
        doTest(
            """
                {exported$caret}

                <script>
                    let existingVariable = 5;
                </script>
            """.trimIndent(),
            """
                {exported}

                <script>
                    $createdStatement

                    let existingVariable = 5;
                </script>
            """.trimIndent()
        )
    }

    override fun testInsideScriptStillWorksJS() {
        doTest(
            """
                <script>
                    let existingVariable = 5;

                    exported$caret;
                </script>
            """.trimIndent(),
            """
                <script>
                    $createdStatement

                    let existingVariable = 5;

                    exported;
                </script>
            """.trimIndent()
        )
    }

    override fun testInsideScriptStillWorksTS() {
        doTest(
            """
                <script lang="ts">
                    let existingVariable = 5;

                    exported$caret
                </script>
            """.trimIndent(),
            """
                <script lang="ts">
                    $createdStatement

                    let existingVariable = 5;

                    exported
                </script>
            """.trimIndent(),
            "Insert 'import {exported} from \"./utils\"'"
        )
    }

    override fun testScriptMissingWithAdjacentModuleScript() {
        doTest(
            """
                <script context="module">
                    let existingVariable = 5;
                </script>

                {exported$caret}
            """.trimIndent(),
            """
                <script context="module">
                    let existingVariable = 5;
                </script>
                <script>
                    $createdStatement
                </script>

                {exported}
            """.trimIndent()
        )
    }

    override fun testInsideModuleScriptWithAdjacentInstanceScript() {
        doTest(
            """
                <script context="module">
                    exported$caret
                </script>
                <script>
                    let existingVariable = 5;
                </script>
            """.trimIndent(),
            """
                <script context="module">
                    $createdStatement

                    exported
                </script>
                <script>
                    let existingVariable = 5;
                </script>
            """.trimIndent()
        )
    }

    fun testComponentBasic() {
        doTest(
            """
                <Hello$caret />
            """.trimIndent(),
            """
                <script>
                    import Hello from "./Hello.svelte";
                </script>

                <Hello />
            """.trimIndent()
        )
    }

    fun testComponentReexport() {
        myFixture.configureByText("barrel.ts", """
            import Hello from "./Hello.svelte";

            export {Hello as RenamedHello};
        """.trimIndent())

        doTest(
            """
                <RenamedHello$caret />
            """.trimIndent(),
            """
                <script>
                    import {RenamedHello} from "./barrel";
                </script>

                <RenamedHello />
            """.trimIndent()
        )
    }

    fun testComponentAddDefaultImportToExistingStatement() {
        doTest(
            """
                <script>
                    import {something} from "./Hello.svelte";
                </script>

                <Hello$caret />
            """.trimIndent(),
            """
                <script>
                    import Hello, {something} from "./Hello.svelte";
                </script>

                <Hello />
            """.trimIndent()
        )
    }

    fun testComponentDynamic() {
        doTest(
            """
                <svelte:component this={Hello$caret}/>
            """.trimIndent(),
            """
                <script>
                    import Hello from "./Hello.svelte";
                </script>

                <svelte:component this={Hello}/>
            """.trimIndent()
        )
    }

    fun testComponentInsideJSFile() {
        myFixture.configureByText(
            "main.js",
            """
                new Hello$caret();
            """.trimIndent()
        )

        myFixture.launchAction(myFixture.findSingleIntention(requestedActionHint))
        myFixture.checkResult(
            """
                import Hello from "./Hello.svelte";

                new Hello();
            """.trimIndent()
        )
    }

    private fun doTest(before: String, after: String, requestedActionHint: String = this.requestedActionHint) {
        myFixture.configureByText("Example.svelte", before)

        myFixture.launchAction(myFixture.findSingleIntention(requestedActionHint))

        myFixture.checkResult(after)
    }

    override fun setUp() {
        super.setUp()
        myFixture.configureByText("utils.ts", """export const exported = 42;""")
        myFixture.configureByText("Hello.svelte", """Hello World""")
        myFixture.enableInspections(
          JSUnresolvedReferenceInspection(),
          TypeScriptUnresolvedVariableInspection(),
          SvelteUnresolvedComponentInspection(),
        )
    }
}

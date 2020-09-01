package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteTypeScriptHighlightingTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
    }

    fun testSimpleWithType() {
        myFixture.configureByText("Usage.svelte",
            """
                <script lang="ts">
                    export let testName: string;

                    declare let test:number;
                    if (test) {
                        test = <error>"hello"</error>;
                    } else {
                        test = 2;
                    }
                    console.log(test);
                </script>
                <title>{testName}</title>
                """.trimIndent())
        myFixture.testHighlighting()
    }
}

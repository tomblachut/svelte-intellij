package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteTypeScriptHighlightingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources"
    override fun getBasePath(): String = "dev/blachut/svelte/lang/codeInsight/tsHighlighting"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
    }

    fun testSimpleWithType() {
        myFixture.configureByText(
            "Usage.svelte",
            """
                <script lang="ts">
                    export let testName: string;
                    console.log(${"$$"}props);
                    declare let test:number;
                    if (test) {
                        test = <error>"hello"</error>;
                    } else {
                        test = 2;
                    }
                    console.log(test);
                </script>
                <title>{testName}</title>
                """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testSimpleImport() {
        myFixture.configureByText("hel.ts", "export const count = 1")
        myFixture.configureByText(
            "Usage.svelte",
            """
                <script lang="ts">
                    import {count} from "./hel";

                    ${"$"}: ${"$"}count && console.log('changed');
                </script>
                <title>test</title>
                """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testTsconfigDirectAncestorDiscovery() {
        val base = basePath + "/" + getTestName(true)
        myFixture.configureByFiles("$base/src/App.svelte", "$base/tsconfig.json")

        myFixture.testHighlighting()
    }

    fun testTsconfigIndirectAncestorDiscovery() {
        val base = basePath + "/" + getTestName(true)
        myFixture.configureByFiles("$base/src/nested/Child.svelte", "$base/tsconfig.json")

        myFixture.testHighlighting()
    }
}

package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteTypeScriptCompletionTest : BasePlatformTestCase() {

    fun testTsImport() {
        myFixture.configureByText("Hello.ts", "export class HelloTest {}")
        myFixture.configureByText("Usage.svelte",
            """
                <script lang="ts">
                    let z = new HelloT<caret>
                </script>
                """.trimIndent())
        myFixture.completeBasic()
        myFixture.checkResult(
            """
                <script lang="ts">
                    import {HelloTest} from "./Hello";

                    let z = new HelloTest()
                </script>
                """.trimIndent())
    }
}

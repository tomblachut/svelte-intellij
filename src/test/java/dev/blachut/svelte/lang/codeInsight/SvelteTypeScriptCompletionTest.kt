package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.impl.JSImportsCoroutineScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking

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
    waitCoroutinesBlocking(JSImportsCoroutineScope.get(project))
    myFixture.checkResult(
      """
                <script lang="ts">
                    import {HelloTest} from "./Hello";

                    let z = new HelloTest()
                </script>
                """.trimIndent())
  }
}

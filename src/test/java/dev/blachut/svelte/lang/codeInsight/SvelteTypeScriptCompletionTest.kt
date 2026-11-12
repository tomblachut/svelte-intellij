package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.impl.JSImportsCoroutineScope
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteTypeScriptCompletionTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
  fun testTsImport() {
    myFixture.configureByText("Hello.ts", "export class HelloTest {}")
    myFixture.configureByText("Usage.svelte",
                              """
                <script lang="ts">
                    let z = new HelloT<caret>
                </script>
                """.trimIndent())
    myFixture.completeBasic()
    waitCoroutinesBlocking(JSImportsCoroutineScope.get(myFixture.project))
    myFixture.checkResult(
      """
                <script lang="ts">
                    import {HelloTest} from "./Hello";

                    let z = new HelloTest()
                </script>
                """.trimIndent())
  }
}

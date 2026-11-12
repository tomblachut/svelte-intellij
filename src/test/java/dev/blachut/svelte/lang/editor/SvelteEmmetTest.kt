package dev.blachut.svelte.lang.editor

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteEmmetTest {
    companion object {
        private val projectFixture = svelteProjectFixture()
    }

    private val myFixture by svelteCodeInsightFixture(projectFixture)

    @Test
    fun testFirstThingEOF(): Unit = doTest(
        """.foo<caret>""",
        """<div class="foo"></div>"""
    )

    @Test
    fun testFirstThing(): Unit = doTest(
        """.foo<caret><div class="existing"></div>""",
        """<div class="foo"></div><div class="existing"></div>"""
    )

    @Test
    fun testFirstIfEOF(): Unit = doTest(
        """if<caret>""",
        """{#if }{/if}"""
    )

    @Test
    fun testFirstEachEOF(): Unit = doTest(
        """each<caret>""",
        """{#each  as }{/each}"""
    )

    @Test
    fun testFirstAwaitEOF(): Unit = doTest(
        """await<caret>""",
        """{#await }{/await}"""
    )

    @Test
    fun testFirstKeyEOF(): Unit = doTest(
        """key<caret>""",
        """{#key }{/key}"""
    )

    @Test
    fun testElse(): Unit = doTest(
        """{#if true}else<caret>{/if}""",
        """{#if true}{:else}{/if}"""
    )

    @Test
    fun testElseIf(): Unit = doTest(
        """{#if true}elseif<caret>{/if}""",
        """{#if true}{:else if }{/if}"""
    )

    @Test
    fun testThen(): Unit = doTest(
        """{#await promise}then<caret>{/await}""",
        """{#await promise}{:then }{/await}"""
    )

    @Test
    fun testCatch(): Unit = doTest(
        """{#await promise}catch<caret>{/await}""",
        """{#await promise}{:catch }{/await}"""
    )

    @Test
    fun testAfterBlockEOF(): Unit = doTest(
        """{#if true}{/if}.foo<caret>""",
        """{#if true}{/if}<div class="foo"></div>"""
    )

    @Test
    fun testAfterBlock(): Unit = doTest(
        """{#if true}{/if}.foo<caret><div class="existing"></div>""",
        """{#if true}{/if}<div class="foo"></div><div class="existing"></div>"""
    )

    @Test
    fun testInsideBlock(): Unit = doTest(
        """{#if true}.foo<caret>{/if}""",
        """{#if true}<div class="foo"></div>{/if}"""
    )

    @Test
    fun testInsideBlockTag(): Unit = doTest(
        """{#if .foo<caret>}{/if}""",
        """{#if .foo   }{/if}"""
    )

    @Test
    fun testAfterBlockStartTag(): Unit = doTest(
        """{#if true}.foo<caret>""",
        """{#if true}<div class="foo"></div>"""
    )

    @Test
    fun testComplexExpression(): Unit = doTest(
        """if>ul.mb-2*2>each>li<caret>""", """
        {#if }
            <ul class="mb-2">
                {#each  as }
                    <li></li>
                {/each}
            </ul>
            <ul class="mb-2">
                {#each  as }
                    <li></li>
                {/each}
            </ul>
        {/if}""".trimIndent()
    )

  private fun doTest(before: String, after: String) {
    myFixture.configureByText("Emmet.svelte", before)
    JSTestUtils.runEmmetTemplate(myFixture)

    myFixture.checkResult(after)
  }
}

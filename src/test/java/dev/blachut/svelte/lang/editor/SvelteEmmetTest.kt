package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteEmmetTest : BasePlatformTestCase() {
    fun testFirstThingEOF() = doTest(
        """.foo<caret>""",
        """<div class="foo"></div>"""
    )

    fun testFirstThing() = doTest(
        """.foo<caret><div class="existing"></div>""",
        """<div class="foo"></div><div class="existing"></div>"""
    )

    fun testFirstIfEOF() = doTest(
        """if<caret>""",
        """{#if }{/if}"""
    )

    fun testFirstEachEOF() = doTest(
        """each<caret>""",
        """{#each  as }{/each}"""
    )

    fun testFirstAwaitEOF() = doTest(
        """await<caret>""",
        """{#await }{/await}"""
    )

    fun testFirstKeyEOF() = doTest(
        """key<caret>""",
        """{#key }{/key}"""
    )

    fun testElse() = doTest(
        """{#if true}else<caret>{/if}""",
        """{#if true}{:else}{/if}"""
    )

    fun testElseIf() = doTest(
        """{#if true}elseif<caret>{/if}""",
        """{#if true}{:else if }{/if}"""
    )

    fun testThen() = doTest(
        """{#await promise}then<caret>{/await}""",
        """{#await promise}{:then }{/await}"""
    )

    fun testCatch() = doTest(
        """{#await promise}catch<caret>{/await}""",
        """{#await promise}{:catch }{/await}"""
    )

    fun testAfterBlockEOF() = doTest(
        """{#if true}{/if}.foo<caret>""",
        """{#if true}{/if}<div class="foo"></div>"""
    )

    fun testAfterBlock() = doTest(
        """{#if true}{/if}.foo<caret><div class="existing"></div>""",
        """{#if true}{/if}<div class="foo"></div><div class="existing"></div>"""
    )

    fun testInsideBlock() = doTest(
        """{#if true}.foo<caret>{/if}""",
        """{#if true}<div class="foo"></div>{/if}"""
    )

    fun testInsideBlockTag() = doTest(
        """{#if .foo<caret>}{/if}""",
        """{#if .foo   }{/if}"""
    )

    fun testAfterBlockStartTag() = doTest(
        """{#if true}.foo<caret>""",
        """{#if true}<div class="foo"></div>"""
    )

    fun testComplexExpression() = doTest(
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
        myFixture.type("\t")
        myFixture.checkResult(after)
    }
}

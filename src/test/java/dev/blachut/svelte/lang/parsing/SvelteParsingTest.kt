package dev.blachut.svelte.lang.parsing

import com.intellij.testFramework.ParsingTestCase
import dev.blachut.svelte.lang.parsing.html.SvelteHTMLParserDefinition
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition

class SvelteParsingTest : ParsingTestCase("dev/blachut/svelte/lang/parsing", "svelte", SvelteJSParserDefinition(), SvelteHTMLParserDefinition()) {
    override fun getTestDataPath(): String = "src/test/resources"

    fun testIfElseIf() = doTest()
    fun testEachAssets() = doTest()
    // TODO Fix tests
//    fun testExpression() = doTest()
//    fun testIncompleteExpression() = doTest()
    fun testWhitespace() = doTest()
    fun testNestedBlocks() = doTest()

    fun testEachAsAsAsAs() = doTest()
    fun testAwaitThenThenThen() = doTest()
    fun testEachAmbiguousAs() = doTest()

    private fun doTest() = doTest(true)
}


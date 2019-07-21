package dev.blachut.svelte.lang

import com.intellij.testFramework.ParsingTestCase

class SvelteParsingTest : ParsingTestCase("", "svelte", SvelteParserDefinition()) {
    override fun getTestDataPath(): String = "src/test/resources/dev/blachut/svelte/lang"

    fun testIfElseIf() = doTest(true)
    fun testEachAssets() = doTest(true)
    fun testExpression() = doTest(true)
    fun testIncompleteExpression() = doTest(true)
    fun testWhitespace() = doTest(true)

    // TODO Improve lexer and enable following tests
//    fun testEachAsAs() = doTest(true)
//    fun testAwaitThenThenThen() = doTest(true)
}


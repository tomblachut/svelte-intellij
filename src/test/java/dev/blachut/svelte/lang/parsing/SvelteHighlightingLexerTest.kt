package dev.blachut.svelte.lang.parsing

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer

class SvelteHighlightingLexerTest : LexerTestCase() {
    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang/parsing"
    override fun getExpectedFileExtension(): String = ".tokens"

    override fun getPathToTestDataFile(extension: String?): String {
        return dirPath + "/" + getTestName(false) + extension
    }

    override fun createLexer(): Lexer {
        return SvelteHtmlHighlightingLexer()
    }

    fun testAwaitThenThenThen() = doTest()
    fun testEachAsAsAsAs() = doTest()
    fun testEachAssets() = doTest()

    fun testExpression() = doTest()

    fun testIf() = doTest()
    fun testIfElseIf() = doTest()

    fun testWhitespace() = doTest()

    private fun doTest() = doFileTest("svelte")
}


package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class SvelteHighlightingLexerTest : LexerTestCase() {
    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang/parsing/html/lexer"
    override fun getExpectedFileExtension(): String = ".tokens"

    override fun getPathToTestDataFile(extension: String?): String {
        return dirPath + "/" + getTestName(false) + extension
    }

    override fun createLexer(): Lexer {
        return SvelteHtmlHighlightingLexer()
    }

    fun testBlockAwaitThenThenThen() = doTest()
    fun testBlockEachAsAsAsAs() = doTest()
    fun testBlockEachAssets() = doTest()
    fun testBlockIfElseIf() = doTest()
    fun testBlockWhitespace() = doTest()

    fun testExpression() = doTest()

//    fun testRestart() = checkCorrectRestartOnEveryToken("""<img alt={{foo: {}}}>""")

    private fun doTest() = doFileTest("svelte")
}


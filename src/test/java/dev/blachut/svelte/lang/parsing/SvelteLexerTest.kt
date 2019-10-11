package dev.blachut.svelte.lang.parsing

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import dev.blachut.svelte.lang.parsing.top.SvelteLexer

class SvelteLexerTest : LexerTestCase() {
    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang/parsing"
    override fun getExpectedFileExtension(): String = ".tokens"

    override fun getPathToTestDataFile(extension: String?): String {
        return dirPath + "/" + getTestName(false) + extension
    }

    override fun createLexer(): Lexer {
        return SvelteLexer()
    }

    fun testIfElseIf() = doTest()
    fun testEachAssets() = doTest()
    fun testExpression() = doTest()
    fun testIncompleteExpression() = doTest()
    fun testWhitespace() = doTest()

    fun testEachAsAsAsAs() = doTest()
    fun testAwaitThenThenThen() = doTest()
    fun testEachAmbiguousAs() = doTest()

    private fun doTest() = doFileTest("svelte")
}


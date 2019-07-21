package dev.blachut.svelte.lang

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class SvelteLexerTest : LexerTestCase() {
    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang"
    override fun getExpectedFileExtension(): String = ".tokens.txt"

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

    // TODO Improve lexer and enable following tests
//    fun testEachAsAs() = doTest(true)
//    fun testAwaitThenThenThen() = doTest(true)

    private fun doTest() = doFileTest("svelte")
}


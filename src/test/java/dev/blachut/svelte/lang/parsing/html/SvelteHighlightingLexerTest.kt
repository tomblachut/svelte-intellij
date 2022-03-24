package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import kotlin.properties.Delegates

class SvelteHighlightingLexerTest : LexerTestCase() {
    private var fixture: IdeaProjectTestFixture by Delegates.notNull()

    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang/parsing/html/lexer"
    override fun getExpectedFileExtension(): String = ".tokens"

    override fun setUp() {
        super.setUp()

        // needed for various XML extension points registration
        fixture = IdeaTestFixtureFactory.getFixtureFactory()
            .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).fixture
        fixture.setUp()
    }

    override fun tearDown() {
        try {
            fixture.tearDown()
        } catch (e: Throwable) {
            addSuppressedException(e)
        } finally {
            super.tearDown()
        }
    }

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

    fun testStyleTagScss() = doTest()

//    fun testRestart() = checkCorrectRestartOnEveryToken("""<img alt={{foo: {}}}>""")

    private fun doTest() = doFileTest("svelte")
}

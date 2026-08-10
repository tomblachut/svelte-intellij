package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import dev.blachut.svelte.lang.getSvelteTestDataPath
import kotlin.properties.Delegates

class SvelteHighlightingLexerTest : LexerTestCase() {
  private var fixture: IdeaProjectTestFixture by Delegates.notNull()

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
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  override fun getDirPath() = "dev/blachut/svelte/lang/parsing/html/lexer"

  override fun getPathToTestDataFile(extension: String): String = getSvelteTestDataPath() + "/$dirPath/" + getTestName(false) + extension

  override fun createLexer(): Lexer {
    return SvelteHtmlLexer(true)
  }

  fun testBlockAwaitThenThenThen() = doTest()
  fun testBlockEachAsAsAsAs() = doTest()
  fun testBlockEachAssets() = doTest()
  fun testBlockIfElseIf() = doTest()
  fun testBlockKey() = doTest()
  fun testBlockSnippet() = doTest()
  fun testBlockWhitespace() = doTest()

  fun testExpression() = doTest()

  fun testCommentInExpression() = doTest()
  fun testBlockCommentInExpression() = doTest()
  fun testEscapedCharInExpression() = doTest()
  fun testRegexLiteralWithQuoteInExpression() = doTest()

  // WEB-77758: `//` and `/* */` comments inside start tags, see
  // https://github.com/sveltejs/svelte/pull/17671
  fun testTagLineComment() = doTest()
  fun testTagBlockComment() = doTest()
  fun testTagBlockCommentUnterminated() = doTest()
  fun testTagCommentInEndTag() = doTest()

  fun testCommentInRawTextExpression() = doTest()
  fun testBlockCommentInRawTextExpression() = doTest()
  fun testEscapedCharInRawTextExpression() = doTest()

  fun testStyleTagScss() = doTest()
  fun testRawText() = doTest()

  // TypeScript in markup lexer tests
  fun testTsContentExpression() = doTest()
  fun testTsAttributeExpression() = doTest()
  fun testTsBlockIf() = doTest()
  fun testTsSnippetParameterTypes() = doTest()
  fun testTsJsCompatibility() = doTest()

  // fun testRestart() = checkCorrectRestartOnEveryToken("""<img alt={{foo: {}}}>""")

  private fun doTest() = doFileTest("svelte")
}

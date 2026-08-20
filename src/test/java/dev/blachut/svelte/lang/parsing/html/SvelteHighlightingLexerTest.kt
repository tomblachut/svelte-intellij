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

  // The `doTest` files above are all checked for correct restart already, see LexerTestCase.doTest;
  // the cases below cover what no test file does.

  fun testRestart() = checkCorrectRestart("""<img alt={{foo: {}}}>""")

  // The lexer stops inside the unterminated `<style>`, leaving `rawTag` set; restarting at the `>` of
  // `<div>` -- a state the lexer reports as restartable -- must not take that over and lex the rest of
  // the file as raw content, see SvelteHtmlBaseLexer.resetTagKind. The token list is asserted as well,
  // because a restart is only checked against this same lexer's full pass.
  fun testRestartRawTag() = doTest(
    "<div>x<style>y",
    """
    XML_START_TAG_START ('<')
    XML_TAG_NAME ('div')
    XML_TAG_END ('>')
    XML_DATA_CHARACTERS ('x')
    XML_START_TAG_START ('<')
    XML_TAG_NAME ('style')
    XML_TAG_END ('>')
    CSS_IDENT ('y')
    """.trimIndent()
  )

  // The attribute expression ends its embedment inside the end tag header, which resumes the base lexer
  // mid-header; that must not lose `isEndTag` and let the `/* */` become a comment token.
  fun testEndTagCommentAfterExpr() = doTest(
    "</div a={x} /* c */>",
    """
    XML_END_TAG_START ('</')
    XML_TAG_NAME ('div')
    TAG_WHITE_SPACE (' ')
    XML_NAME ('a')
    XML_EQ ('=')
    START_MUSTACHE ('{')
    JS:IDENTIFIER ('x')
    END_MUSTACHE ('}')
    TAG_WHITE_SPACE (' ')
    XML_DATA_CHARACTERS ('/*')
    TAG_WHITE_SPACE (' ')
    XML_DATA_CHARACTERS ('c')
    TAG_WHITE_SPACE (' ')
    XML_DATA_CHARACTERS ('*/>')
    """.trimIndent()
  )

  private fun doTest() = doFileTest("svelte")
}

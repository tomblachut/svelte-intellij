package dev.blachut.svelte.lang.parsing.html

import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.getSvelteTestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import kotlin.io.path.Path
import kotlin.io.path.readText

// The test application is needed for various XML extension points registration
@TestApplication
class SvelteHighlightingLexerTest {
  private val lexer by lexerFixture(
    "${getSvelteTestDataPath()}/dev/blachut/svelte/lang/parsing/html/lexer",
    expectedFileExtension = ".tokens",
  ) {
    SvelteHtmlLexer(true)
  }

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = testInfo.testMethod.get().name.removePrefix("test")
  }

  @Test
  fun testBlockAwaitThenThenThen() = doTest()
  @Test
  fun testBlockEachAsAsAsAs() = doTest()
  @Test
  fun testBlockEachAssets() = doTest()
  @Test
  fun testBlockIfElseIf() = doTest()
  @Test
  fun testBlockKey() = doTest()
  @Test
  fun testBlockSnippet() = doTest()
  @Test
  fun testBlockWhitespace() = doTest()

  @Test
  fun testExpression() = doTest()

  @Test
  fun testCommentInExpression() = doTest()
  @Test
  fun testBlockCommentInExpression() = doTest()
  @Test
  fun testEscapedCharInExpression() = doTest()
  @Test
  fun testRegexLiteralWithQuoteInExpression() = doTest()

  // WEB-77758: `//` and `/* */` comments inside start tags, see
  // https://github.com/sveltejs/svelte/pull/17671
  @Test
  fun testTagLineComment() = doTest()
  @Test
  fun testTagBlockComment() = doTest()
  @Test
  fun testTagBlockCommentUnterminated() = doTest()
  @Test
  fun testTagCommentInEndTag() = doTest()

  @Test
  fun testCommentInRawTextExpression() = doTest()
  @Test
  fun testBlockCommentInRawTextExpression() = doTest()
  @Test
  fun testEscapedCharInRawTextExpression() = doTest()

  @Test
  fun testStyleTagScss() = doTest()
  @Test
  fun testRawText() = doTest()

  // TypeScript in markup lexer tests
  @Test
  fun testTsContentExpression() = doTest()
  @Test
  fun testTsAttributeExpression() = doTest()
  @Test
  fun testTsBlockIf() = doTest()
  @Test
  fun testTsSnippetParameterTypes() = doTest()
  @Test
  fun testTsJsCompatibility() = doTest()

  // The `doTest` files above are all checked for correct restart already, see LexerTestCase.doTest;
  // the cases below cover what no test file does.

  @Test
  fun testRestart() = lexer.checkCorrectRestart("""<img alt={{foo: {}}}>""")

  // The lexer stops inside the unterminated `<style>`, leaving `rawTag` set; restarting at the `>` of
  // `<div>` -- a state the lexer reports as restartable -- must not take that over and lex the rest of
  // the file as raw content, see SvelteHtmlBaseLexer.resetTagKind. The token list is asserted as well,
  // because a restart is only checked against this same lexer's full pass.
  @Test
  fun testRestartRawTag() = lexer.doTest(
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
  @Test
  fun testEndTagCommentAfterExpr() = lexer.doTest(
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

  private fun doTest() {
    val path = "${getSvelteTestDataPath()}/dev/blachut/svelte/lang/parsing/html/lexer/$testName"
    val text = StringUtil.convertLineSeparators(Path("$path.svelte").readText().trim())
    PlatformTestUtil.assertSameLinesWithFile("$path.tokens", lexer.printTokens(text, 0))
    lexer.checkCorrectRestart(text)
  }
}

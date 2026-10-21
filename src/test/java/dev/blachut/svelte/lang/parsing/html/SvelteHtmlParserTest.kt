package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition
import dev.blachut.svelte.lang.parsing.ts.SvelteTypeScriptParserDefinition
import org.jetbrains.plugins.scss.parser.SCSSParserDefinition
import org.junit.jupiter.api.Test

@TestApplication
class SvelteHtmlParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(
    projectFixture,
    getSvelteTestDataPath() + "/dev/blachut/svelte/lang/parsing/html/parser",
    "svelte",
    false,
    SvelteHTMLParserDefinition(),
    SvelteJSParserDefinition(),
    SvelteTypeScriptParserDefinition(),
    JavascriptParserDefinition(),
    CSSParserDefinition(),
    SCSSParserDefinition()
  )

  @Test
  fun testAttributeQuoted() = doTest()
  @Test
  fun testAttributeQuotedExpressionWithQuotes() = doTest()
  @Test
  fun testAttributeShorthand() = doTest()
  @Test
  fun testAttributeSpread() = doTest()
  @Test
  fun testAttributeUnquoted() = doTest()

  @Test
  fun testAttachSimple() = doTest()
  @Test
  fun testAttachWithParams() = doTest()
  @Test
  fun testAttachInline() = doTest()
  @Test
  fun testAttachMultiple() = doTest()
  @Test
  fun testAttachConditional() = doTest()
  @Test
  fun testAttachWithSpread() = doTest()
  @Test
  fun testAttachErrorRecovery() = doTest()
  @Test
  fun testAttachTernary() = doTest()
  @Test
  fun testAttachInlineWithCleanup() = doTest()

  @Test
  fun testBlockAwaitCatch() = doTest()
  @Test
  fun testBlockAwaitCatchWithoutVariable() = doTest()
  @Test
  fun testBlockAwaitInlineCatchWithoutVariable() = doTest()
  @Test
  fun testBlockAwaitInlineThenWithoutVariable() = doTest()
  @Test
  fun testBlockAwaitThenThenThen() = doTest()
  @Test
  fun testBlockAwaitThenWithoutVariable() = doTest()
  @Test
  fun testBlockEachAmbiguousAs() = doTest()
  @Test
  fun testBlockEachAsAsAsAs() = doTest()
  @Test
  fun testBlockEachAssets() = doTest()
  @Test
  fun testBlockEachWithoutAs() = doTest()
  @Test
  fun testBlockIfElseIf() = doTest()
  @Test
  fun testBlockKey() = doTest()
  @Test
  fun testBlockSnippet() = doTest()
  @Test
  fun testBlockNesting() = doTest()
  @Test
  fun testBlockWhitespace() = doTest()

  @Test
  fun testExpression() = doTest()
  @Test
  fun testExpressionIncomplete() = doTest()

  @Test
  fun testHtmlEntityInExpression() = doTest()

  @Test
  fun testConstTagVariable() = doTest()
  @Test
  fun testConstTag() = doTest()
  @Test
  fun testLetTag() = doTest()

  @Test
  fun testHtmlAutoClosingTags() = doTest()
  @Test
  fun testHtmlAutoClosingTagsAcrossBlock() = doTest()
  @Test
  fun testHtmlAutoClosingTagsInsideBlock() = doTest()

  @Test
  fun testHtmlClosingTagMatchesNothing1() = doTest()
  @Test
  fun testHtmlClosingTagMatchesNothing2() = doTest()
  @Test
  fun testHtmlClosingTagMatchesNothing3() = doTest()

  @Test
  fun testHtmlMissingEndTags() = doTest()
  @Test
  fun testHtmlNamelessClosingTag() = doTest()
  @Test
  fun testHtmlSpecialTags() = doTest()

  @Test
  fun testHtmlUnclosed1() = doTest()
  @Test
  fun testHtmlUnclosed2() = doTest()
  @Test
  fun testHtmlUnclosed3() = doTest()

  @Test
  fun testLet() = doTest()

  @Test
  fun testQuoteBalanceScriptComment() = doTest()
  @Test
  fun testQuoteBalanceUnclosedStringLiteral() = doTest()

  @Test
  fun testCommentInExpression() = doTest()
  @Test
  fun testBlockCommentInExpression() = doTest()

  // WEB-77758: `//` and `/* */` comments inside start tags, see
  // https://github.com/sveltejs/svelte/pull/17671
  @Test
  fun testTagLineComment() = doTest()
  @Test
  fun testTagLineCommentBeforeAttribute() = doTest()
  @Test
  fun testTagLineCommentOnly() = doTest()
  @Test
  fun testTagLineCommentTrailing() = doTest()
  @Test
  fun testTagLineCommentAdjacent() = doTest()
  @Test
  fun testTagLineCommentSwallowsTagEnd() = doTest()
  @Test
  fun testTagBlockComment() = doTest()
  @Test
  fun testTagBlockCommentAdjacent() = doTest()
  @Test
  fun testTagBlockCommentUnterminated() = doTest()
  @Test
  fun testTagCommentInEndTag() = doTest()

  @Test
  fun testEscapedCharInExpression() = doTest()
  @Test
  fun testRegexLiteralWithQuoteInExpression() = doTest()
  @Test
  fun testCommentInRawTextExpression() = doTest()
  @Test
  fun testBlockCommentInRawTextExpression() = doTest()
  @Test
  fun testEscapedCharInRawTextExpression() = doTest()

  @Test
  fun testScriptGenerics() = doTest()
  @Test
  fun testScriptGenericsMultiple() = doTest()
  @Test
  fun testScriptGenericsSimple() = doTest()
  @Test
  fun testScriptGenericsDefault() = doTest()
  @Test
  fun testScriptGenericsMalformed() = doTest()
  @Test
  fun testScriptGenericsObjectConstraint() = doTest()
  @Test
  fun testScriptGenericsComplexConstraint() = doTest()
  @Test
  fun testScriptGenericsLegacySyntax() = doTest()

  @Test
  fun testScriptGenericsTrailingComma() = doTest()
  @Test
  fun testScriptGenericsLongList() = doTest()
  @Test
  fun testScriptGenericsTupleConstraint() = doTest()
  @Test
  fun testScriptGenericsConditionalType() = doTest()
  @Test
  fun testScriptGenericsUnionConstraint() = doTest()
  @Test
  fun testScriptGenericsKeyof() = doTest()
  @Test
  fun testScriptGenericsNestedAngleBrackets() = doTest()
  @Test
  fun testScriptGenericsMultipleDefaults() = doTest()

  @Test
  fun testStoreReferences() = doTest()

  // TypeScript in markup tests
  @Test
  fun testTsLangModeDetection() = doTest()
  @Test
  fun testTsContentExpression() = doTest()
  @Test
  fun testTsAttributeExpression() = doTest()
  @Test
  fun testTsAttributeSpread() = doTest()
  @Test
  fun testTsBlockIf() = doTest()
  @Test
  fun testTsBlockEach() = doTest()
  @Test
  fun testTsBlockAwait() = doTest()
  @Test
  fun testTsBlockKey() = doTest()
  @Test
  fun testTsBlockSnippet() = doTest()
  @Test
  fun testTsSnippetParameterTypes() = doTest()
  @Test
  fun testTsSnippetWithConstAndNonNull() = doTest()
  @Test
  fun testTsConstTag() = doTest()
  @Test
  fun testTsDeclarationTags() = doTest()
  @Test
  fun testTsJsCompatibility() = doTest()
  @Test
  fun testTsMixedScripts() = doTest()
  @Test
  fun testTsDirectiveBind() = doTest()
  @Test
  fun testTsBlockEachTopLevelAs() = doTest()
  @Test
  fun testTsBlockIfTopLevelAs() = doTest()
  @Test
  fun testTsMarkupExpressions() = doTest()
  @Test
  fun testTsComprehensive() = doTest()
  @Test
  fun testTsAttachSimple() = doTest()

  @Test
  fun testStyleTagDefault() = doTest()
  @Test
  fun testStyleTagScss() = doTest()
  @Test
  fun testStyleAttribute() = doTest()
  @Test
  fun testStyleAttributeWithExpressionEnd() = doTest()
  @Test
  fun testRawText() = doTest()

  @Test
  fun testStyleReparse() {
    parsing.doReparseTest("<style global></styl\n", "<style global></style\n")
    parsing.doReparseTest("<style global></styl\n", "<style global></style><div\n")
    parsing.doReparseTest("<style global>.foo { }</styl\n", "<style global>.foo { }</style\n")
    parsing.doReparseTest("<style global>.foo { }</styl\n", "<style global>.foo { }</style><div\n")
  }

  @Test
  fun testScriptReparse() {
    parsing.doReparseTest("<script></scrip\n", "<script></script\n")
    parsing.doReparseTest("<script></scrip\n", "<script></script><div\n")
    parsing.doReparseTest("<script>$: foo = 2;</scrip\n", "<script>$: foo = 2;</script\n")
    parsing.doReparseTest("<script>$: foo = 2;</scrip\n", "<script>$: foo = 2;</script><div\n")
  }

  @Test
  fun testLangReparseJsToTs() {
    val jsText = """
      <script lang="js">
        let count = 0;
      </script>

      {#if count > 0}
        <p>{count}</p>
      {/if}
    """.trimIndent()
    parsing.doReparseTest(jsText, jsText.replace("js", "ts"))
  }

  @Test
  fun testLangReparseTsToJs() {
    val tsText = """
      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count > 0}
        <p>{count}</p>
      {/if}
    """.trimIndent()
    parsing.doReparseTest(tsText, tsText.replace("ts", "js"))
  }

  private fun doTest(): Unit = parsing.doTest(true)
}

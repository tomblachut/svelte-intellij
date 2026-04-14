package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.JSHtmlEmbeddedContentSupport
import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.ParserDefinition
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.javascript.JSElementTypeServiceHelper.registerJSElementTypeServices
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XmlASTFactory
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider
import com.intellij.psi.css.CssHtmlEmbeddedContentSupport
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.xml.StartTagEndTokenProvider
import com.intellij.testFramework.ParsingTestCase
import com.intellij.xml.testFramework.XmlElementTypeServiceHelper.registerXmlElementTypeServices
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition
import dev.blachut.svelte.lang.parsing.ts.SvelteTypeScriptParserDefinition
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.scss.ScssTokenTypesProvider
import org.jetbrains.plugins.scss.parser.SCSSParserDefinition
import org.jetbrains.plugins.scss.psi.SCSSTreeElementFactory

class SvelteHtmlParserTest : ParsingTestCase(
  "dev/blachut/svelte/lang/parsing/html/parser",
  "svelte",
  SvelteHTMLParserDefinition(),
  SvelteJSParserDefinition(),
  SvelteTypeScriptParserDefinition(),
  JavascriptParserDefinition(),
  CSSParserDefinition(),
  SCSSParserDefinition()
) {
  override fun getTestDataPath(): String = getSvelteTestDataPath()

  override fun setUp() {
    super.setUp()

    addExplicitExtension(LanguageASTFactory.INSTANCE, SvelteHTMLLanguage.INSTANCE, SvelteHtmlASTFactory())
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, XmlASTFactory())
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
    addExplicitExtension(LanguageASTFactory.INSTANCE, SCSSLanguage.INSTANCE, SCSSTreeElementFactory())

    registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider::class.java)
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java)

    registerExtensions(
      EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java,
      listOf(
        CssEmbeddedTokenTypesProvider(),
        ScssTokenTypesProvider()
      )
    )
    HtmlEmbeddedContentSupport.register(application, testRootDisposable,
                                        CssHtmlEmbeddedContentSupport::class.java, JSHtmlEmbeddedContentSupport::class.java,
                                        SvelteHtmlEmbeddedContentSupport::class.java)

    registerXmlElementTypeServices(application, testRootDisposable)
    registerJSElementTypeServices(application, testRootDisposable)

    //        registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
    //        registerExtension(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
    //        application.registerService(
    //            CssElementDescriptorFactory2::class.java,
    //            CssElementDescriptorFactory2("css-parsing-tests.xml")
    //        )

    //        registerExtensionPoint(FrameworkIndexingHandler.EP_NAME, FrameworkIndexingHandlerEP::class.java)
  }

  override fun configureFromParserDefinition(definition: ParserDefinition, extension: String?) {
    super.configureFromParserDefinition(definition, extension)
  }

  fun testAttributeQuoted() = doTest()
  fun testAttributeShorthand() = doTest()
  fun testAttributeSpread() = doTest()
  fun testAttributeUnquoted() = doTest()

  fun testAttachSimple() = doTest()
  fun testAttachWithParams() = doTest()
  fun testAttachInline() = doTest()
  fun testAttachMultiple() = doTest()
  fun testAttachConditional() = doTest()
  fun testAttachWithSpread() = doTest()
  fun testAttachErrorRecovery() = doTest()
  fun testAttachTernary() = doTest()
  fun testAttachInlineWithCleanup() = doTest()

  fun testBlockAwaitCatch() = doTest()
  fun testBlockAwaitCatchWithoutVariable() = doTest()
  fun testBlockAwaitInlineCatchWithoutVariable() = doTest()
  fun testBlockAwaitInlineThenWithoutVariable() = doTest()
  fun testBlockAwaitThenThenThen() = doTest()
  fun testBlockAwaitThenWithoutVariable() = doTest()
  fun testBlockEachAmbiguousAs() = doTest()
  fun testBlockEachAsAsAsAs() = doTest()
  fun testBlockEachAssets() = doTest()
  fun testBlockEachWithoutAs() = doTest()
  fun testBlockIfElseIf() = doTest()
  fun testBlockKey() = doTest()
  fun testBlockSnippet() = doTest()
  fun testBlockNesting() = doTest()
  fun testBlockWhitespace() = doTest()

  fun testExpression() = doTest()
  fun testExpressionIncomplete() = doTest()

  fun testHtmlEntityInExpression() = doTest()

  fun testConstTagVariable() = doTest()

  fun testHtmlAutoClosingTags() = doTest()
  fun testHtmlAutoClosingTagsAcrossBlock() = doTest()
  fun testHtmlAutoClosingTagsInsideBlock() = doTest()

  fun testHtmlClosingTagMatchesNothing1() = doTest()
  fun testHtmlClosingTagMatchesNothing2() = doTest()
  fun testHtmlClosingTagMatchesNothing3() = doTest()

  fun testHtmlMissingEndTags() = doTest()
  fun testHtmlNamelessClosingTag() = doTest()
  fun testHtmlSpecialTags() = doTest()

  fun testHtmlUnclosed1() = doTest()
  fun testHtmlUnclosed2() = doTest()
  fun testHtmlUnclosed3() = doTest()

  fun testLet() = doTest()

  fun testQuoteBalanceScriptComment() = doTest()
  fun testQuoteBalanceUnclosedStringLiteral() = doTest()

  fun testScriptGenerics() = doTest()
  fun testScriptGenericsMultiple() = doTest()
  fun testScriptGenericsSimple() = doTest()
  fun testScriptGenericsDefault() = doTest()
  fun testScriptGenericsMalformed() = doTest()
  fun testScriptGenericsObjectConstraint() = doTest()
  fun testScriptGenericsComplexConstraint() = doTest()
  fun testScriptGenericsLegacySyntax() = doTest()

  fun testScriptGenericsTrailingComma() = doTest()
  fun testScriptGenericsLongList() = doTest()
  fun testScriptGenericsTupleConstraint() = doTest()
  fun testScriptGenericsConditionalType() = doTest()
  fun testScriptGenericsUnionConstraint() = doTest()
  fun testScriptGenericsKeyof() = doTest()
  fun testScriptGenericsNestedAngleBrackets() = doTest()
  fun testScriptGenericsMultipleDefaults() = doTest()

  fun testStoreReferences() = doTest()

  // TypeScript in markup tests
  fun testTsLangModeDetection() = doTest()
  fun testTsContentExpression() = doTest()
  fun testTsAttributeExpression() = doTest()
  fun testTsAttributeSpread() = doTest()
  fun testTsBlockIf() = doTest()
  fun testTsBlockEach() = doTest()
  fun testTsBlockAwait() = doTest()
  fun testTsBlockKey() = doTest()
  fun testTsBlockSnippet() = doTest()
  fun testTsSnippetParameterTypes() = doTest()
  fun testTsSnippetWithConstAndNonNull() = doTest()
  fun testTsConstTag() = doTest()
  fun testTsJsCompatibility() = doTest()
  fun testTsMixedScripts() = doTest()
  fun testTsDirectiveBind() = doTest()
  fun testTsBlockEachTopLevelAs() = doTest()
  fun testTsBlockIfTopLevelAs() = doTest()
  fun testTsMarkupExpressions() = doTest()
  fun testTsComprehensive() = doTest()
  fun testTsAttachSimple() = doTest()

  fun testStyleTagDefault() = doTest()
  fun testStyleTagScss() = doTest()
  fun testStyleAttribute() = doTest()
  fun testStyleAttributeWithExpressionEnd() = doTest()
  fun testRawText() = doTest()

  fun testStyleReparse() {
    doReparseTest("<style global></styl\n", "<style global></style\n")
    doReparseTest("<style global></styl\n", "<style global></style><div\n")
    doReparseTest("<style global>.foo { }</styl\n", "<style global>.foo { }</style\n")
    doReparseTest("<style global>.foo { }</styl\n", "<style global>.foo { }</style><div\n")
  }

  fun testScriptReparse() {
    doReparseTest("<script></scrip\n", "<script></script\n")
    doReparseTest("<script></scrip\n", "<script></script><div\n")
    doReparseTest("<script>$: foo = 2;</scrip\n", "<script>$: foo = 2;</script\n")
    doReparseTest("<script>$: foo = 2;</scrip\n", "<script>$: foo = 2;</script><div\n")
  }

  fun testLangReparseJsToTs() {
    val jsText = """
      <script lang="js">
        let count = 0;
      </script>

      {#if count > 0}
        <p>{count}</p>
      {/if}
    """.trimIndent()
    doReparseTest(jsText, jsText.replace("js", "ts"))
  }

  fun testLangReparseTsToJs() {
    val tsText = """
      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count > 0}
        <p>{count}</p>
      {/if}
    """.trimIndent()
    doReparseTest(tsText, tsText.replace("ts", "js"))
  }

  private fun doTest() = doTest(true)
}

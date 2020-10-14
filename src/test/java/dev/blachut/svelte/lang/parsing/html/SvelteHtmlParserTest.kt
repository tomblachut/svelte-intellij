package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XmlASTFactory
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider
import com.intellij.psi.css.CssRulesetBlockEmbeddedTokenTypesProvider
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.xml.StartTagEndTokenProvider
import com.intellij.testFramework.ParsingTestCase
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition
import dev.blachut.svelte.lang.parsing.js.SvelteJSScriptContentProvider
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.scss.ScssTokenTypesProvider
import org.jetbrains.plugins.scss.parser.SCSSParserDefinition
import org.jetbrains.plugins.scss.psi.SCSSTreeElementFactory

class SvelteHtmlParserTest : ParsingTestCase(
    "dev/blachut/svelte/lang/parsing/html/parser",
    "svelte",
    SvelteHTMLParserDefinition(),
    SvelteJSParserDefinition(),
    JavascriptParserDefinition(),
    CSSParserDefinition(),
    SCSSParserDefinition()
) {
    override fun getTestDataPath(): String = "src/test/resources"

    override fun setUp() {
        super.setUp()

        addExplicitExtension(LanguageASTFactory.INSTANCE, SvelteHTMLLanguage.INSTANCE, SvelteHtmlASTFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, XmlASTFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, SCSSLanguage.INSTANCE, SCSSTreeElementFactory())

        addExplicitExtension(
            LanguageHtmlScriptContentProvider.INSTANCE,
            SvelteJSLanguage.INSTANCE,
            SvelteJSScriptContentProvider()
        )

        registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider::class.java)
        registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java)

        registerExtensions(
            EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java,
            listOf(
                CssEmbeddedTokenTypesProvider(),
                CssRulesetBlockEmbeddedTokenTypesProvider(),
                ScssTokenTypesProvider()
            )
        )

//        registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
//        registerExtension(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
//        application.registerService(
//            CssElementDescriptorFactory2::class.java,
//            CssElementDescriptorFactory2("css-parsing-tests.xml")
//        )

//        registerExtensionPoint(FrameworkIndexingHandler.EP_NAME, FrameworkIndexingHandlerEP::class.java)
    }

    fun testAttributeQuoted() = doTest()
    fun testAttributeShorthand() = doTest()
    fun testAttributeSpread() = doTest()
    fun testAttributeUnquoted() = doTest()

    fun testBlockAwaitCatch() = doTest()
    fun testBlockAwaitThenThenThen() = doTest()
    fun testBlockEachAmbiguousAs() = doTest()
    fun testBlockEachAsAsAsAs() = doTest()
    fun testBlockEachAssets() = doTest()
    fun testBlockIfElseIf() = doTest()
    fun testBlockNesting() = doTest()
    fun testBlockWhitespace() = doTest()

    fun testExpression() = doTest()
    fun testExpressionIncomplete() = doTest()

    fun testHtmlAutoClosingTags() = doTest()
    fun testHtmlAutoClosingTagsAcrossBlock() = doTest()
    fun testHtmlAutoClosingTagsInsideBlock() = doTest()

    fun testHtmlClosingTagMatchesNothing1() = doTest()
    fun testHtmlClosingTagMatchesNothing2() = doTest()
    fun testHtmlClosingTagMatchesNothing3() = doTest()

    fun testHtmlMissingEndTags() = doTest()
    fun testHtmlSpecialTags() = doTest()

    fun testHtmlUnclosed1() = doTest()
    fun testHtmlUnclosed2() = doTest()
    fun testHtmlUnclosed3() = doTest()

    fun testLet() = doTest()

    fun testQuoteBalanceScriptComment() = doTest()
    fun testQuoteBalanceUnclosedStringLiteral() = doTest()

    fun testStyleTagDefault() = doTest()
    fun testStyleTagScss() = doTest()
    fun testStyleAttribute() = doTest()
    fun testStyleAttributeWithExpressionEnd() = doTest()

    private fun doTest() = doTest(true)
}

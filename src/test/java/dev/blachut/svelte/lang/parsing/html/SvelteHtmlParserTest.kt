package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XmlASTFactory
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.xml.StartTagEndTokenProvider
import com.intellij.testFramework.ParsingTestCase
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition

class SvelteHtmlParserTest : ParsingTestCase(
    "dev/blachut/svelte/lang/parsing/html/parser",
    "svelte",
    SvelteHTMLParserDefinition(),
    SvelteJSParserDefinition(),
    CSSParserDefinition()
) {
    override fun getTestDataPath(): String = "src/test/resources"

    override fun setUp() {
        super.setUp()

        addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, XmlASTFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, SvelteHTMLLanguage.INSTANCE, SvelteHtmlASTFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())

        registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider::class.java)
        registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java)
    }

    fun testAttributeQuoted() = doTest()
    fun testAttributeShorthand() = doTest()
    fun testAttributeSpread() = doTest()
    fun testAttributeUnquoted() = doTest()

    //  TODO Support await...catch blocks
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

    fun testHtmlMissingEndTags() = doTest()
    fun testHtmlSpecialTags() = doTest()

    fun testLet() = doTest()

    private fun doTest() = doTest(true)
}

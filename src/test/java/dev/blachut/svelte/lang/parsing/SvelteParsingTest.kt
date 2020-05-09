package dev.blachut.svelte.lang.parsing

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
import dev.blachut.svelte.lang.parsing.html.SvelteHTMLParserDefinition
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlASTFactory
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition

class SvelteParsingTest : ParsingTestCase(
    "dev/blachut/svelte/lang/parsing",
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

    fun testAwaitThenThenThen() = doTest()
    fun testEachAmbiguousAs() = doTest()
    fun testEachAsAsAsAs() = doTest()
    fun testEachAssets() = doTest()

    fun testExpression() = doTest()
    fun testExpressionIncomplete() = doTest()

    fun testIf() = doTest()
    fun testIfElseIf() = doTest()

    fun testNestedBlocks() = doTest()
    fun testWhitespace() = doTest()

    fun testHtmlMissingEndTags() = doTest()
    fun testHtmlSpecialTags() = doTest()
    // await catch

    private fun doTest() = doTest(true)
}

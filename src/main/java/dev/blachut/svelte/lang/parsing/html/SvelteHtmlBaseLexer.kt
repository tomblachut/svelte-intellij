package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlBaseLexer(private val assumeExternalBraces: Boolean = false)
    : MergingLexerAdapter(FlexAdapter(_SvelteHtmlLexer()), TOKENS_TO_MERGE) {

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        var correctedState = initialState
        if (assumeExternalBraces && initialState == _SvelteHtmlLexer.YYINITIAL) {
            correctedState = _SvelteHtmlLexer.SVELTE_INTERPOLATION_START
        }
        super.start(buffer, startOffset, endOffset, correctedState)
    }

    val flexLexer: _SvelteHtmlLexer
        get() = (original as FlexAdapter).flex as _SvelteHtmlLexer
}

private val TOKENS_TO_MERGE = TokenSet.create(
    XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE, XmlTokenType.XML_REAL_WHITE_SPACE,
    XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS,
    XmlTokenType.XML_TAG_CHARACTERS,
    SvelteTokenTypes.CODE_FRAGMENT
)

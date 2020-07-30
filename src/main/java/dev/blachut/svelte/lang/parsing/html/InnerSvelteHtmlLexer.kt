package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class InnerSvelteHtmlLexer : MergingLexerAdapter(FlexAdapter(_SvelteHtmlLexer()), TOKENS_TO_MERGE) {
    val flexLexer: _SvelteHtmlLexer
        get() = (original as FlexAdapter).flex as _SvelteHtmlLexer
}

internal val TOKENS_TO_MERGE = TokenSet.create(
    XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE, XmlTokenType.XML_REAL_WHITE_SPACE,
    XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS,
    XmlTokenType.XML_TAG_CHARACTERS,
    SvelteTokenTypes.CODE_FRAGMENT
)

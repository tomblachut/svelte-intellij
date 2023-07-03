package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlRawTextLexer : LayeredLexer(MergingLexerAdapter(FlexAdapter(_SvelteHtmlRawTextLexer()),
                                                   TokenSet.create(XmlTokenType.XML_DATA_CHARACTERS, SvelteTokenTypes.CODE_FRAGMENT))) {
  init {
    registerLayer(JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX), SvelteTokenTypes.CODE_FRAGMENT)
  }
}
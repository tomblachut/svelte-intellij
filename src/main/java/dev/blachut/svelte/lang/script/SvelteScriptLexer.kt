package dev.blachut.svelte.lang.script

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang._SvelteScriptIdentifierLexer

class SvelteScriptLexer() : LayeredLexer(JSFlexAdapter(DialectOptionHolder.ECMA_6)) {
    init {
        val identifierLexer = MergingLexerAdapter(FlexAdapter(_SvelteScriptIdentifierLexer(null)), TokenSet.create(JSTokenTypes.IDENTIFIER))
        registerLayer(identifierLexer, JSTokenTypes.IDENTIFIER)
    }
}

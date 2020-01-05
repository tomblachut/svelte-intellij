package dev.blachut.svelte.lang.parsing.top

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteLexer : MergingLexerAdapter(
    FlexAdapter(_SvelteLexer(null)),
    TokenSet.create(SvelteTokenTypes.HTML_FRAGMENT, SvelteTokenTypes.CODE_FRAGMENT)
)

package dev.blachut.svelte.lang

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.psi.SvelteTypes

internal class SvelteLexer : MergingLexerAdapter(
        FlexAdapter(_SvelteLexer(null)),
        TokenSet.create(SvelteTypes.HTML_FRAGMENT, SvelteTypes.CODE_FRAGMENT)
)

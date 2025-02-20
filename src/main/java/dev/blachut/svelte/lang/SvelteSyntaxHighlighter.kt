package dev.blachut.svelte.lang

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlLexer
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

internal class SvelteSyntaxHighlighter : HtmlFileHighlighter() {
  override fun getHighlightingLexer(): Lexer {
    return SvelteHtmlLexer(true)
  }

  override fun getTokenHighlights(tokenType: IElementType): Array<out TextAttributesKey> {
    return when (tokenType) {
      SvelteTokenTypes.START_MUSTACHE,
      SvelteTokenTypes.END_MUSTACHE,

      JSTokenTypes.SHARP,
      JSTokenTypes.COLON,
      JSTokenTypes.DIV,
      JSTokenTypes.AT,

        // Only keywords not covered by other means // todo revisit after optimising lexing+parsing
      SvelteTokenTypes.EACH_KEYWORD,
      SvelteTokenTypes.KEY_KEYWORD,
      SvelteTokenTypes.SNIPPET_KEYWORD,
      -> KEYWORDS

      else -> super.getTokenHighlights(tokenType)
    }
  }

  companion object {
    private val KEYWORD = createTextAttributesKey("SVELTE_KEYWORD", JSHighlighter.JS_KEYWORD)
    private val KEYWORDS = pack(KEYWORD)
  }
}

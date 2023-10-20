package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlRawTextTagContentProvider
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteHtmlHighlightingLexer : HtmlHighlightingLexer(SvelteHtmlBaseLexer(), false) {

  override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean =
    provider::class != HtmlScriptStyleEmbeddedContentProvider::class
    && provider::class != HtmlRawTextTagContentProvider::class

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
  }

  override fun isPossiblyCustomTagName(tagName: CharSequence): Boolean {
    return isSvelteComponentTag(tagName)
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet =
    SvelteHtmlLexer.TAG_EMBEDMENT_START_TOKENS

}

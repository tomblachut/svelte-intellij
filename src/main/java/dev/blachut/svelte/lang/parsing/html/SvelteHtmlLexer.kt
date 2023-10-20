package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.HtmlRawTextTagContentProvider
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlLexer(highlightMode: Boolean) : HtmlLexer(SvelteHtmlBaseLexer(), false, highlightMode) {

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    if (isHighlighting) {
      super.start(buffer, startOffset, endOffset, initialState)
    }
    else {
      // TODO Verify if those masks don't clash with ones used in BaseHtmlLexer.initState
      val baseState = initialState and 0xffff
      val nestingLevel = initialState shr 16
      (delegate as SvelteHtmlBaseLexer).flexLexer.bracesNestingLevel = nestingLevel
      super.start(buffer, startOffset, endOffset, baseState)
    }
  }

  override fun getState(): Int {
    if (isHighlighting) {
      return super.getState()
    } else {
      val nestingLevel = (delegate as SvelteHtmlBaseLexer).flexLexer.bracesNestingLevel
      return (nestingLevel shl 16) or (super.getState() and 0xffff)
    }
  }

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
    TAG_EMBEDMENT_START_TOKENS

  companion object {
    val TAG_EMBEDMENT_START_TOKENS = TokenSet.orSet(BaseHtmlLexer.TAG_EMBEDMENT_START_TOKENS,
                                                    TokenSet.create(SvelteTokenTypes.START_MUSTACHE))
  }
}

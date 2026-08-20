package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType

class SvelteHtmlBaseLexer(private val assumeExternalBraces: Boolean = false)
  : MergingLexerAdapter(FlexAdapter(_SvelteHtmlLexer()), TOKENS_TO_MERGE) {

  private val flexLexer: _SvelteHtmlLexer
    get() = (delegate as FlexAdapter).flex as _SvelteHtmlLexer

  /**
   * Forgets which kind of tag header is being lexed.
   *
   * `rawTag` and `isEndTag` are fields of the generated lexer rather than part of its state, and the JFlex
   * `reset()` behind [FlexAdapter.start] leaves them untouched. Without this, a lexer restarted from a
   * stored state inherits the tag kind from wherever the previous pass happened to stop, and a stale
   * `rawTag == 1` turns the `>` of an ordinary tag into the start of raw content.
   *
   * Only call this when starting a *new* pass, i.e. from [SvelteHtmlLexer.start]. Zero is the correct value
   * at every offset the highlighter may restart at, but it is wrong in the middle of a tag header, which is
   * where `BaseHtmlLexer.restartAfterEmbedment` resumes this lexer after each attribute expression.
   */
  internal fun resetTagKind() {
    flexLexer.apply {
      rawTag = 0
      isEndTag = 0
    }
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    var correctedState = initialState
    if (assumeExternalBraces && initialState == _SvelteHtmlLexer.YYINITIAL) {
      correctedState = _SvelteHtmlLexer.SVELTE_INTERPOLATION_START
    }
    super.start(buffer, startOffset, endOffset, correctedState)
  }

}

private val TOKENS_TO_MERGE = TokenSet.create(
  XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE, XmlTokenType.XML_REAL_WHITE_SPACE,
  XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS,
  XmlTokenType.XML_TAG_CHARACTERS
)

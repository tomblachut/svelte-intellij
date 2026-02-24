package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.RestartableLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteLangMode

/**
 * Wraps SvelteHtmlLexer to:
 * 1. Track language mode during lexing (detecting <script lang="ts">)
 * 2. Emit a zero-length marker token at the end encoding the detected mode
 *
 * This allows the parser to know the language mode before parsing any expressions.
 */
class SvelteParsingLexer(
  private val delegateLexer: SvelteHtmlLexer,
  private val parentLangMode: SvelteLangMode? = null,
) : DelegateLexer(delegateLexer), RestartableLexer {

  private var additionalState = BASE_LEXING

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    additionalState = initialState and 0b11
    delegateLexer.lexedLangMode = parentLangMode ?: SvelteLangMode.entries[initialState shr SHIFT_1 and 0b11]
    super.start(buffer, startOffset, endOffset, initialState shr SHIFT_2)

    if (additionalState != BASE_LEXING) return
    checkPendingLangMode()
  }

  override fun getState(): Int {
    val delegateState = super.getState()
    val langModeState = delegateLexer.lexedLangMode.ordinal
    return (delegateState shl SHIFT_2) or (langModeState shl SHIFT_1) or additionalState
  }

  override fun advance() {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      additionalState = ADDITIONAL_TOKEN_LEXED
    }
    if (additionalState != BASE_LEXING) return

    super.advance()
    checkPendingLangMode()
  }

  private fun checkPendingLangMode() {
    if (parentLangMode != null) return // do not emit additional token for nested lexers

    val baseToken = super.getTokenType()
    if (baseToken == null) {
      // delegate lexer has finished - emit marker token
      if (delegateLexer.lexedLangMode == SvelteLangMode.PENDING) {
        delegateLexer.lexedLangMode = SvelteLangMode.NO_TS
      }
      additionalState = ADDITIONAL_TOKEN_LEXING
    }
  }

  override fun getTokenType(): IElementType? {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      return delegateLexer.lexedLangMode.astMarkerToken
    }
    return super.getTokenType()
  }

  /** Language mode after lexing completes */
  val lexedLangMode: SvelteLangMode
    get() {
      if (parentLangMode != null) return parentLangMode
      if (additionalState != ADDITIONAL_TOKEN_LEXED) error("can't use lexedLangMode before lexing complete")
      return delegateLexer.lexedLangMode
    }

  override fun getStartState(): Int {
    return 0
  }

  override fun isRestartableState(state: Int): Boolean {
    return (delegate as? RestartableLexer)?.isRestartableState(state shr SHIFT_2) ?: true
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: com.intellij.lexer.TokenIterator?) {
    start(buffer, startOffset, endOffset, initialState)
  }

  companion object {
    private const val SHIFT_1 = 2
    private const val SHIFT_2 = SHIFT_1 + 2

    private const val BASE_LEXING = 0
    private const val ADDITIONAL_TOKEN_LEXING = 1
    private const val ADDITIONAL_TOKEN_LEXED = 2
  }
}

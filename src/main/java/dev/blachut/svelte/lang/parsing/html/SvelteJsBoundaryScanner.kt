package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes

object SvelteJsBoundaryScanner {
  /**
   * Find the offset of the unbalanced `}` that terminates a Svelte interpolation.
   *
   * @param buf       the full character buffer
   * @param start     offset to start scanning (typically right after the opening `{`)
   * @param end       maximum offset to scan (typically buffer end)
   * @return offset of the unbalanced `}`, or [end] if no unbalanced `}` was found
   *         (interpolation reaches EOF — caller treats this as an unterminated expression
   *         and emits a CODE_FRAGMENT spanning [start, end])
   */
  fun findUnbalancedRbrace(buf: CharSequence, start: Int, end: Int): Int {
    // initialState 0 = _JavaScriptLexer.YYINITIAL (fresh expression context)
    val js = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    js.start(buf, start, end, 0)
    var depth = 0
    while (js.tokenType != null) {
      when (js.tokenType) {
        // JSX expression containers `{...}` use XML_LBRACE/XML_RBRACE — intentionally
        // NOT counted here; they're balanced internally by JSX rules and don't affect
        // the Svelte interpolation boundary.
        JSTokenTypes.LBRACE -> depth++
        JSTokenTypes.RBRACE -> {
          if (depth == 0) return js.tokenStart
          depth--
        }
      }
      js.advance()
    }
    // No unbalanced `}` found — unterminated expression. Caller emits CODE_FRAGMENT
    // spanning [start, end) and the lexer reaches EOF without END_MUSTACHE.
    return end
  }
}

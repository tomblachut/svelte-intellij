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
   *         (interpolation reaches EOF — caller treats this as an unterminated
   *         expression and emits a CODE_FRAGMENT spanning [start, end])
   */
  fun findUnbalancedRbrace(buf: CharSequence, start: Int, end: Int): Int {
    // Mirror the real Svelte compiler's `tag()` entry dispatch
    // (packages/svelte/src/compiler/phases/1-parse/state/tag.js): skip
    // whitespace, then skip a leading `/` unless it's the start of a `//`
    // line comment or `/*` block comment. Without this skip,
    // `JSFlexAdapter(JS_WITH_JSX)` in YYINITIAL would lex a leading `/` as
    // the start of a regex literal and consume the buffer until the next
    // `/` or EOF — wrong for Svelte block-close markers like `{/snippet}`,
    // `{/if}`, and also for any malformed `{/<non-identifier>}` shape the
    // real compiler routes to its block-close parser.
    var scanFrom = start
    while (scanFrom < end && isHostFlexWhitespace(buf[scanFrom])) scanFrom++
    if (scanFrom + 1 < end && buf[scanFrom] == '/') {
      val next = buf[scanFrom + 1]
      if (next != '/' && next != '*') scanFrom++
    }

    val js = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    js.start(buf, scanFrom, end, 0)
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
    return end
  }

  /**
   * Matches the host JFlex's `WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+`
   * definition. Keeping this aligned ensures the scanner skips exactly what the
   * host lexer would consume as whitespace before invoking us.
   */
  private fun isHostFlexWhitespace(c: Char): Boolean = when (c) {
    ' ', '\t', '\n', '\r', '\u000C', '\u0085', '\u2028', '\u2029' -> true
    else -> false
  }
}

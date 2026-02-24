package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

/**
 * TypeScript expression lexer for Svelte expressions when <script lang="ts"> is present.
 * Similar to [SvelteJSExpressionLexer] but uses TypeScript dialect.
 */
class SvelteTSExpressionLexer(assumeExternalBraces: Boolean) : LayeredLexer(SvelteHtmlBaseLexer(assumeExternalBraces)) {
  init {
    registerLayer(JSFlexAdapter(DialectOptionHolder.TS), SvelteTokenTypes.CODE_FRAGMENT)
  }
}

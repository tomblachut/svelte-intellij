package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSHighlightingHandlersFactory
import com.intellij.lang.javascript.highlighting.JSKeywordHighlighterVisitor

/**
 * Highlighting handlers factory for Svelte markup expressions.
 *
 * This factory is registered for both SvelteJS and SvelteTS dialects in plugin.xml.
 * Using the same factory for both ensures consistent Svelte-specific keyword highlighting
 * (e.g., `as`, `then`, `html`, `debug`, `const`) regardless of the file's language mode,
 * and allows highlighting to work correctly when users switch between JS and TS.
 */
class SvelteHighlightingHandlersFactory : JSHighlightingHandlersFactory() {
  override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder, dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor {
    return SvelteKeywordHighlighterVisitor(holder)
  }
}
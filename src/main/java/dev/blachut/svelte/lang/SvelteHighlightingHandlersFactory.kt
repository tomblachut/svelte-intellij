package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSHighlightingHandlersFactory
import com.intellij.lang.javascript.highlighting.JSKeywordHighlighterVisitor

class SvelteHighlightingHandlersFactory: JSHighlightingHandlersFactory() {
  override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder, dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor {
    return SvelteKeywordHighlighterVisitor(holder)
  }
}
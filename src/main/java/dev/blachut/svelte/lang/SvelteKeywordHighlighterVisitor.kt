package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.validation.ES6KeywordHighlighterVisitor
import dev.blachut.svelte.lang.psi.SvelteInitialTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyPsiElement
import dev.blachut.svelte.lang.psi.SvelteVisitor

class SvelteKeywordHighlighterVisitor(holder: HighlightInfoHolder) : ES6KeywordHighlighterVisitor(holder), SvelteVisitor {
    override fun visitInitialTag(tag: SvelteInitialTag) {
        highlightChildKeywordOfType(tag, JSTokenTypes.AS_KEYWORD)
        // Direct children should be safe to treat as identifiers
        highlightChildKeywordOfType(tag, JSTokenTypes.IDENTIFIER) // then keyword
        super.visitInitialTag(tag)
    }

    override fun visitLazyElement(element: SvelteJSLazyPsiElement) {
        // Direct children should be safe to treat as identifiers
        highlightChildKeywordOfType(element, JSTokenTypes.IDENTIFIER) // debug, html

        super.visitLazyElement(element)
    }
}

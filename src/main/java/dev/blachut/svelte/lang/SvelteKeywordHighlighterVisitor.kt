package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.validation.TypeScriptKeywordHighlighterVisitor
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.psi.SvelteInitialTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyPsiElement
import dev.blachut.svelte.lang.psi.SvelteTokenTypes
import dev.blachut.svelte.lang.psi.SvelteVisitor

class SvelteKeywordHighlighterVisitor(holder: HighlightInfoHolder) : TypeScriptKeywordHighlighterVisitor(holder),
    SvelteVisitor {
    override fun visitInitialTag(tag: SvelteInitialTag) {
        highlightChildKeywordOfType(tag, SvelteTokenTypes.AS_KEYWORD)
        highlightChildKeywordOfType(tag, SvelteTokenTypes.THEN_KEYWORD)
        super.visitInitialTag(tag)
    }

    override fun visitLazyElement(element: SvelteJSLazyPsiElement) {
        highlightChildKeywordOfType(element, SvelteTokenTypes.HTML_KEYWORD)
        highlightChildKeywordOfType(element, SvelteTokenTypes.DEBUG_KEYWORD)

        super.visitLazyElement(element)
    }

    override fun visitJSLabeledStatement(node: JSLabeledStatement) {
        if (node.label == SvelteReactiveDeclarationsUtil.REACTIVE_LABEL) {
            highlightChildKeywordOfType(node, JSTokenTypes.IDENTIFIER)
        }

        super.visitJSLabeledStatement(node)
    }
}

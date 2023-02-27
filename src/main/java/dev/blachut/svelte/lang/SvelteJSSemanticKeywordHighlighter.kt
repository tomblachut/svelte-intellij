package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.validation.JSKeywordHighlighterVisitor
import com.intellij.lang.javascript.validation.JSSemanticKeywordHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteJSSemanticKeywordHighlighter : JSSemanticKeywordHighlighter() {

    override fun suitableForFile(file: PsiFile): Boolean {
        return file is SvelteHtmlFile
    }

    override fun createKeywordHighlightingVisitor(owner: PsiElement, holder: HighlightInfoHolder): JSKeywordHighlighterVisitor {
        val factory = JSAnalysisHandlersFactory.forLanguage(SvelteJSLanguage.INSTANCE)
        return factory.createKeywordHighlighterVisitor(holder, SvelteJSLanguage.INSTANCE.optionHolder)
    }

    override fun clone(): HighlightVisitor {
        return SvelteJSSemanticKeywordHighlighter()
    }
}

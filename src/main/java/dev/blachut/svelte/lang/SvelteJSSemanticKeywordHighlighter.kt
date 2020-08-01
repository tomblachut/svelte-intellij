package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.validation.JSSemanticKeywordHighlighter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import java.util.*

class SvelteJSSemanticKeywordHighlighter(private val myFile: PsiFile, document: Document) : JSSemanticKeywordHighlighter(myFile, document), DumbAware {
    private val myHolder: HighlightInfoHolder = HighlightInfoHolder(myFile)

    override fun doCollectInformation(progress: ProgressIndicator) {
        myHolder.clear()
        if (myFile is SvelteHtmlFile) {
            val factory = JSAnalysisHandlersFactory.forLanguage(SvelteJSLanguage.INSTANCE)
            myFile.acceptChildren(factory.createKeywordHighlighterVisitor(myHolder, SvelteJSLanguage.INSTANCE.optionHolder))
        }
    }

    override fun doApplyInformationToEditor() {
        val highlights: MutableList<HighlightInfo?> = ArrayList()

        for (i in 0 until myHolder.size()) {
            highlights.add(myHolder[i])
        }

        // TODO After dropping 2020.1: unwrap conditional
        @Suppress("SENSELESS_COMPARISON")
        if (myDocument != null) {
            UpdateHighlightersUtil.setHighlightersToEditor(
                myProject,
                myDocument,
                0,
                myFile.textLength,
                highlights,
                colorsScheme,
                id
            )
        }
    }
}

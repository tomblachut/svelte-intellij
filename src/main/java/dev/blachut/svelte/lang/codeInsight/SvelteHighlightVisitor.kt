package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteFile

class SvelteHighlightVisitor(private var myHolder: HighlightInfoHolder? = null) : HighlightVisitor {
    override fun suitableForFile(file: PsiFile): Boolean {
        return file is SvelteFile
    }

    override fun analyze(file: PsiFile, updateWholeFile: Boolean, holder: HighlightInfoHolder, action: Runnable): Boolean {
        myHolder = holder
        try {
            action.run()
        } finally {
            myHolder = null
        }
        return true
    }

    override fun clone(): HighlightVisitor {
        return SvelteHighlightVisitor()
    }

    override fun visit(element: PsiElement) {
        if (element is JSLabeledStatement && element.text.startsWith("$:")) {
            val start = element.textOffset
            val highlightInfo = HighlightInfoType.HighlightInfoTypeImpl(HighlightSeverity.INFORMATION, JSHighlighter.JS_KEYWORD)
            val info = HighlightInfo.newHighlightInfo(highlightInfo).range(start, start + 1).descriptionAndTooltip("Svelte Label").create()
            myHolder?.add(info)
        }
    }

}

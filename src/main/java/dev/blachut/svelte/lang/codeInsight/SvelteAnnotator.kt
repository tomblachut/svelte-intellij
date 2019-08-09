package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.SvelteFileViewProvider

class SvelteAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is JSLabeledStatement && element.text.startsWith("$:") && element.containingFile.viewProvider is SvelteFileViewProvider) {
            if (holder is AnnotationHolderImpl) {
                holder.removeIf { it.textAttributes.externalName == "JS.LABEL" }
                val start = element.textOffset
                val range = TextRange.from(start, 2)
                val annotation = holder.createInfoAnnotation(range, "Svelte Label")
                annotation.textAttributes = JSHighlighter.JS_KEYWORD
            }
        }
    }
}

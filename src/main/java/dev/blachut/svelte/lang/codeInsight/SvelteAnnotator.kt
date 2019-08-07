package dev.blachut.svelte.lang.codeInsight

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
            val start = element.textOffset
            val range = TextRange.from(start, 1)
            val annotation = holder.createInfoAnnotation(range, "Svelte Label")
            annotation.textAttributes = JSHighlighter.JS_KEYWORD
        }
    }
}

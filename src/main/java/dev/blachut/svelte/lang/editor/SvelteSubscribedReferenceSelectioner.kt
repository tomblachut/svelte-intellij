package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteSubscribedReferenceSelectioner : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        return e.elementType == JSTokenTypes.IDENTIFIER && SvelteJSReferenceExpression.isDollarPrefixedName(e.text)
    }

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor
    ): List<TextRange> {
        val originalTextRange = e.textRange
        val list = mutableListOf(originalTextRange)
        list.add(TextRange.create(originalTextRange.startOffset + 1, originalTextRange.endOffset))

        return list
    }

    override fun getMinimalTextRangeLength(element: PsiElement, text: CharSequence, cursorOffset: Int): Int {
        return element.textLength - 1
    }
}

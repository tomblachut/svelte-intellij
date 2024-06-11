package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.parsing.js.isSingleDollarPrefixedName

class SvelteSubscribedReferenceSelectioner : ExtendWordSelectionHandlerBase() {
  override fun canSelect(e: PsiElement): Boolean {
    return isSvelteSubscribedReferenceIdentifier(e)
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

internal fun isSvelteSubscribedReferenceIdentifier(e: PsiElement): Boolean {
  return isSvelteContext(e) && e.elementType == JSTokenTypes.IDENTIFIER && isSingleDollarPrefixedName(e.text)
}

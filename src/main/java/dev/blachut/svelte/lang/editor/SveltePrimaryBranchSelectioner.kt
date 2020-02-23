package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.blocks.SveltePrimaryBranch

class SveltePrimaryBranchSelectioner : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        return e is SveltePrimaryBranch
    }

    override fun getMinimalTextRangeLength(element: PsiElement, text: CharSequence, cursorOffset: Int): Int {
        return element.parent.textLength
    }
}

package dev.blachut.svelte.lang.editor

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteSubscribedReferenceSelectionFilter : Condition<PsiElement> {
    override fun value(e: PsiElement): Boolean {
        return !(e.elementType == JSTokenTypes.IDENTIFIER && SvelteJSReferenceExpression.isDollarPrefixedName(e.text))
    }
}

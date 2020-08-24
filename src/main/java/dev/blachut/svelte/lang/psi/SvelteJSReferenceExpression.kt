package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.refactoring.rename.FragmentaryPsiReference

class SvelteJSReferenceExpression(elementType: IElementType) : JSReferenceExpressionImpl(elementType),
    FragmentaryPsiReference {
    val isSubscribedReference: Boolean
        get() = qualifier == null && super.getReferencedName()?.let(::isDollarPrefixedName) ?: false

    override fun isReadOnlyFragment(): Boolean {
        return false
    }

    override fun isFragmentOnlyRename(): Boolean {
        return isSubscribedReference
    }

    override fun getReferencedName(): String? {
        val name = super.getReferencedName()
        return if (name != null && qualifier == null && isDollarPrefixedName(name)) name.substring(1) else name
    }

    override fun getCanonicalText(): String {
        val name = super.getCanonicalText()
        return if (isDollarPrefixedName(name)) name.substring(1) else name
    }

    override fun getRangeInElement(): TextRange {
        val range = super.getRangeInElement()
        return if (isSubscribedReference) TextRange(range.startOffset + 1, range.endOffset) else range
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val correctedName = if (isSubscribedReference) "\$$newElementName" else newElementName
        return super.handleElementRename(correctedName)
    }

    companion object {
        fun isDollarPrefixedName(name: String): Boolean {
            return name.length > 1 && name[0] == '$' && name[1] != '$'
        }
    }
}

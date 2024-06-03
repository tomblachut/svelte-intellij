package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.codeInsight.isSingleDollarPrefixedName
import dev.blachut.svelte.lang.codeInsight.removeSingleDollarPrefixUnchecked

/**
 *  References with names only starting with a single $ character. They might resolve to either a store, or a rune.
 */
class SvelteJSReferenceExpression(elementType: IElementType) : JSReferenceExpressionImpl(elementType) {
  fun isStoreSubscription(): Boolean {
    val target = resolve()
    return target is JSNamedElement && !isSingleDollarPrefixedName(target.name)
  }

  override fun isReferenceToElement(element: PsiElement): Boolean {
    val referencedName = referencedName
    return JSReferenceUtil.isReferenceTo(this, referencedName, element) ||
           JSReferenceUtil.isReferenceTo(this, referencedName?.let { removeSingleDollarPrefixUnchecked(it) }, element)
  }

  /**
   * Primary Rename refactoring handler, used by [bindToElement]
   */
  override fun getNameToBind(element: PsiNamedElement): String? {
    val newElementName = super.getNameToBind(element) ?: return null
    if (isSingleDollarPrefixedName(newElementName)) return newElementName
    return "\$$newElementName"
  }

  /**
   * Secondary Rename refactoring handler.
   *
   * Called rarely by the platform and some other JS code, and we don't have a test for it.
   *
   * @see [bindToElement]
   */
  override fun handleElementRename(newElementName: String): PsiElement {
    val correctedName = if (isStoreSubscription()) "\$$newElementName" else newElementName
    return super.handleElementRename(correctedName)
  }
}

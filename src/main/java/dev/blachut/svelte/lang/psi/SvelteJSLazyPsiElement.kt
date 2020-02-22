package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType

/**
 * Implementing JSEmbeddedContent is crucial for detecting custom JS dialect and associating JavaScript extensions
 *
 * @see com.intellij.lang.javascript.DialectDetector.calculateJSLanguage
  */
class SvelteJSLazyPsiElement(type: IElementType, text: CharSequence) : LazyParseablePsiElement(type, text), JSEmbeddedContent {
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is SvelteVisitor) {
            visitor.visitLazyElement(this)
        }

        super.accept(visitor)
    }

    override fun toString(): String {
        return "SvelteJS: $elementType"
    }
}

package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType

/**
 * Implementing JSEmbeddedContent is crucial for associating JavaScript extensions and code insight to work
 *
 * @see com.intellij.lang.javascript.DialectDetector.calculateJSLanguage
  */
class SvelteJSLazyPsiElement(type: IElementType, text: CharSequence) : LazyParseablePsiElement(type, text), JSEmbeddedContent {
    override fun toString(): String {
        return "SvelteJS: $elementType"
    }
}

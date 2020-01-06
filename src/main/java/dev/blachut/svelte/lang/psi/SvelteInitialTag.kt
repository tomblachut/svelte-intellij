package dev.blachut.svelte.lang.psi

import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType

class SvelteInitialTag(type: IElementType, text: CharSequence) : LazyParseablePsiElement(type, text) {
    override fun toString(): String {
        return "SvelteInitialTag($elementType)"
    }
}

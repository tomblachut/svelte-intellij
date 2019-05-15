package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

open class SveltePsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun toString(): String {
        return "Svelte: " + node.elementType.toString()
    }
}
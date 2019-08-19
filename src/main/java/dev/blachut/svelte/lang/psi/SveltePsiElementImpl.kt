package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil

open class SveltePsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun toString(): String {
        return StringUtil.trimEnd(javaClass.simpleName, "Impl")
    }
}

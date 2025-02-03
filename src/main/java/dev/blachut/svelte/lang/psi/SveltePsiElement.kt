package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

open class SveltePsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {
  override fun toString(): String {
    return javaClass.simpleName.removeSuffix("Impl")
  }
}

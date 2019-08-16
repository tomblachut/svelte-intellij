package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.psi.PsiElement

object SvelteJSElementTypes {
    val PARAMETER = object : JSParameterElementType("EMBEDDED_PARAMETER") {
        override fun construct(node: ASTNode): PsiElement? {
            return SvelteJSParameter(node)
        }
    }
}


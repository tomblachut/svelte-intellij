package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.contextOfType
import dev.blachut.svelte.lang.psi.SveltePsiElement
import dev.blachut.svelte.lang.psi.SvelteTag

sealed class SvelteBranch(node: ASTNode) : SveltePsiElement(node), JSElement {
    val tag get() = firstChild as SvelteTag
    val fragment get() = lastChild as SvelteFragment

    @Suppress("UNUSED_PARAMETER")
    protected fun processParameterDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        if (lastParent != null && (lastParent != tag || place.contextOfType<SvelteTagDependentExpression>() != null)) {
            var result = true
            tag.acceptChildren(object : JSDestructuringVisitor() {
                override fun visitJSParameter(node: JSParameter) {
                    if (result && !processor.execute(node, ResolveState.initial())) {
                        result = false
                    }
                }

                override fun visitJSVariable(node: JSVariable) {}
            })
            return result
        }

        return true
    }
}

abstract class SveltePrimaryBranch(node: ASTNode) : SvelteBranch(node)

abstract class SvelteAuxiliaryBranch(node: ASTNode) : SvelteBranch(node)

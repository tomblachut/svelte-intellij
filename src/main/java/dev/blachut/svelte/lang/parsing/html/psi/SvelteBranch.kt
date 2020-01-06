package dev.blachut.svelte.lang.parsing.html.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl
import dev.blachut.svelte.lang.psi.SvelteTag

sealed class SvelteBranch(node: ASTNode) : SveltePsiElementImpl(node), JSElement {
    val tag: SvelteTag get() = firstChild as SvelteTag

    @Suppress("UNUSED_PARAMETER")
    protected fun processParameterDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        if (lastParent != null) {
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

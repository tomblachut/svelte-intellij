package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.impl.JSParameterImpl
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope

class SvelteJSParameter(node: ASTNode) : JSParameterImpl(node) {
    override fun getUseScope(): SearchScope {
        return LocalSearchScope.EMPTY
    }
}

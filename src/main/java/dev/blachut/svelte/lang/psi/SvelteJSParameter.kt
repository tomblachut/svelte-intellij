package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.impl.JSParameterImpl
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil

class SvelteJSParameter(node: ASTNode) : JSParameterImpl(node) {
    override fun getUseScope(): SearchScope {
        val tag = PsiTreeUtil.getParentOfType(this, SvelteOpeningTag::class.java, SvelteContinuationTag::class.java)
        tag ?: return LocalSearchScope.EMPTY
        return LocalSearchScope(tag.parent)
    }
}

package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.impl.JSParameterImpl
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.blocks.SvelteBranch

class SvelteJSParameter : JSParameterImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: JSParameterStub, elementType: JSParameterElementType) : super(stub, elementType)

    override fun getUseScope(): SearchScope {
        val tag = PsiTreeUtil.getParentOfType(this, SvelteBranch::class.java, XmlTag::class.java)
        tag ?: return LocalSearchScope.EMPTY
        return LocalSearchScope(tag)
    }
}

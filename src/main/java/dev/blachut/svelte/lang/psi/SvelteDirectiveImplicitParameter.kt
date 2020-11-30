package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.resolve.ImplicitJSVariableImpl
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.blocks.SvelteBranch

// TODO implement JSParameter, align with SvelteJSParameter
class SvelteDirectiveImplicitParameter(name: String, provider: PsiElement) :
    ImplicitJSVariableImpl(name, JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.JS, false), provider) {
    override fun canNavigate(): Boolean {
        return true
    }

    override fun getTextOffset(): Int {
        return parent.textOffset
    }

    override fun isLocal(): Boolean = true

    override fun hasBlockScope(): Boolean = false

    override fun getUseScope(): SearchScope {
        val tag = PsiTreeUtil.getParentOfType(this, SvelteBranch::class.java, XmlTag::class.java)
        tag ?: return LocalSearchScope.EMPTY
        return LocalSearchScope(tag)
    }
}

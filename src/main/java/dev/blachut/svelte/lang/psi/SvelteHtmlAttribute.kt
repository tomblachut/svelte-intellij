package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.DefaultRoleFinder
import com.intellij.psi.tree.RoleFinder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement

class SvelteHtmlAttribute : XmlAttributeImpl(SvelteHtmlElementTypes.SVELTE_HTML_ATTRIBUTE) {
    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement,
    ): Boolean {
        if (!name.startsWith("let:")) return true
        val value = valueElement ?: return true
        val parameter = PsiTreeUtil.findChildOfType(value, SvelteJSParameter::class.java) ?: return true

        var result = true
        parameter.accept(object : JSDestructuringVisitor() {
            override fun visitJSParameter(node: JSParameter) {
                if (result && !processor.execute(node, ResolveState.initial())) {
                    result = false
                }
            }

            override fun visitJSVariable(node: JSVariable) {}
        })
        return result
    }

    override fun getNameElement(): XmlElement {
        if (firstChild is SveltePsiElement) {
            return this
        }

        return super.getNameElement()
    }

    override fun getName(): String {
        if (firstChild !is SveltePsiElement) {
            return super.getName()
        }

        val jsNode = SPREAD_OR_SHORTHAND_FINDER.findChild(firstChildNode) ?: return ""

        return if (jsNode.firstChildNode.elementType === JSElementTypes.SPREAD_EXPRESSION) {
            "<spread>"
        } else {
            jsNode.text
        }
    }

    override fun toString(): String {
        return "SvelteHtmlAttribute: $name"
    }

    companion object {
        val SPREAD_OR_SHORTHAND_FINDER: RoleFinder = DefaultRoleFinder(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)
    }
}

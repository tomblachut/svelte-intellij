package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.IXmlTagElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlUtil
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteHtmlTagElementType(debugName: String) : IElementType(debugName, SvelteHTMLLanguage.INSTANCE), IXmlTagElementType

val SVELTE_HTML_TAG = SvelteHtmlTagElementType("SVELTE_HTML_TAG")

// Check XmlTagImpl.createDelegate && HtmlTagDelegate if something breaks. Esp. HtmlTagDelegate.findSubTags
class SvelteHtmlTag : XmlTagImpl(SVELTE_HTML_TAG), HtmlTag {
    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        for (attribute in attributes) {
            if (!attribute.name.startsWith("let:")) continue
            val value = attribute.valueElement ?: continue
            val parameter = PsiTreeUtil.findChildOfType(value, SvelteJSParameter::class.java) ?: continue

            var result = true
            parameter.accept(object : JSDestructuringVisitor() {
                override fun visitJSParameter(node: JSParameter) {
                    if (result && !processor.execute(node, ResolveState.initial())) {
                        result = false
                    }
                }

                override fun visitJSVariable(node: JSVariable) {}
            })

            if (!result) {
                return false
            }
        }

        return true
    }

    override fun isCaseSensitive(): Boolean {
        return true
    }

    override fun getRealNs(value: String?): String? {
        return if (XmlUtil.XHTML_URI == value) XmlUtil.HTML_URI else value
    }

    override fun toString(): String {
        return "SvelteHtmlTag: $name"
    }

    override fun getParentTag(): XmlTag? {
        return PsiTreeUtil.getParentOfType(this, XmlTag::class.java)
    }

    // Copied from HTML
    override fun getNamespaceByPrefix(prefix: String): String {
        val xmlNamespace = super.getNamespaceByPrefix(prefix)
        if (prefix.isNotEmpty()) {
            return xmlNamespace
        }
        return if (xmlNamespace.isEmpty() || xmlNamespace == XmlUtil.XHTML_URI) {
            XmlUtil.HTML_URI
        } else xmlNamespace
        // ex.: mathML and SVG namespaces can be used inside html file
    }
}

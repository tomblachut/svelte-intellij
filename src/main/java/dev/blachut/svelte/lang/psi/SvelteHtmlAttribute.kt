package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.tree.DefaultRoleFinder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.RoleFinder
import com.intellij.psi.xml.IXmlAttributeElementType
import com.intellij.psi.xml.XmlElement
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteHtmlAttributeElementType(debugName: String) : IElementType(debugName, SvelteHTMLLanguage.INSTANCE), IXmlAttributeElementType

val SVELTE_HTML_ATTRIBUTE = SvelteHtmlAttributeElementType("SVELTE_HTML_ATTRIBUTE")

val SPREAD_OR_SHORTHAND_FINDER: RoleFinder = DefaultRoleFinder(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)

class SvelteHtmlAttribute : XmlAttributeImpl(SVELTE_HTML_ATTRIBUTE) {
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
}

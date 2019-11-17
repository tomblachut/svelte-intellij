package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteXmlExtension : HtmlXmlExtension() {
    override fun isAvailable(file: PsiFile): Boolean = file.language is SvelteHTMLLanguage

    /**
     * Whether writing self closing `<tag/>` is correct
     */
    override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
        return isSvelteComponentTag(tag.name) || tag.name == "slot" || super.isSelfClosingTagAllowed(tag)
    }

    /**
     * Whether should warn when writing `<tag></tag>` with empty body, with quick fix that replaces it with `<tag/>`
     */
    override fun isCollapsibleTag(tag: XmlTag): Boolean {
        return isSvelteComponentTag(tag.name) || tag.name == "slot" || super.isCollapsibleTag(tag)
    }

    /**
     * Whether tag follows stricter rules of XML. Single tags are e.g. <p> & <li>, they don't require closing tag
     */
    override fun isSingleTagException(tag: XmlTag): Boolean = isSvelteComponentTag(tag.name) || tag.name == "slot"

    override fun getAttributeValuePresentation(tag: XmlTag?, attributeName: String, defaultAttributeQuote: String): AttributeValuePresentation {
        if (attributeName == "slot") {
            return super.getAttributeValuePresentation(tag, attributeName, defaultAttributeQuote)
        }

        return object : AttributeValuePresentation {
            override fun getPrefix(): String = "{"
            override fun getPostfix(): String = "}"
        }
    }
}

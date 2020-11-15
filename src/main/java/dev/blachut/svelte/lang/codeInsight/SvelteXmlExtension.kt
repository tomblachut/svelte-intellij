package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.directives
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteXmlExtension : HtmlXmlExtension() {
    // TODO expand this list
    private val collapsibleTags = setOf("slot", "style", "script")

    override fun isAvailable(file: PsiFile): Boolean = file.language is SvelteHTMLLanguage

    /**
     * Whether writing self closing `<tag/>` is correct
     */
    override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
        return isSvelteComponentTag(tag.name) || collapsibleTags.contains(tag.name) || super.isSelfClosingTagAllowed(tag)
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

    override fun createTagNameReference(nameElement: ASTNode, startTagFlag: Boolean): TagNameReference? {
        return if (isSvelteComponentTag(nameElement.text)) {
            SvelteTagNameReference(nameElement, startTagFlag)
        } else {
            super.createTagNameReference(nameElement, startTagFlag)
        }
    }

    override fun getAttributeValuePresentation(
        tag: XmlTag?,
        attributeName: String,
        defaultAttributeQuote: String
    ): AttributeValuePresentation {
        if (attributeName == "slot") {
            return super.getAttributeValuePresentation(tag, attributeName, defaultAttributeQuote)
        }

        val prefix = if (attributeName.contains(':')) attributeName.split(":").firstOrNull() else null
        if (prefix != null && directives.contains(prefix)) {
            return object : AttributeValuePresentation {
                override fun getPrefix(): String = "{"
                override fun getPostfix(): String = "}"
            }
        }

        // non-directive attribute
        // TODO Return empty presentation after auto matching {} and ""
        return super.getAttributeValuePresentation(tag, attributeName, defaultAttributeQuote)
    }
}

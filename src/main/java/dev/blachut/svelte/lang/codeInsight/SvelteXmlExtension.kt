package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteXmlExtension : HtmlXmlExtension() {
    override fun isAvailable(file: PsiFile): Boolean = file.language is SvelteHTMLLanguage

    override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
        return isSvelteComponentTag(tag.name) || super.isSelfClosingTagAllowed(tag)
    }

    override fun isCollapsibleTag(tag: XmlTag): Boolean {
        return isSvelteComponentTag(tag.name) || super.isCollapsibleTag(tag)
    }

    override fun isSingleTagException(tag: XmlTag): Boolean = isSvelteComponentTag(tag.name)

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

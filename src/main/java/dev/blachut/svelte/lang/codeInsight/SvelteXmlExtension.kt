package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteXmlExtension : HtmlXmlExtension() {
    override fun isAvailable(file: PsiFile?): Boolean = file?.language is SvelteHTMLLanguage

    override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
        return tag.descriptor is SvelteComponentTagDescriptor || super.isSelfClosingTagAllowed(tag)
    }

    override fun isSingleTagException(tag: XmlTag): Boolean = isSvelteComponentTag(tag.name)
}

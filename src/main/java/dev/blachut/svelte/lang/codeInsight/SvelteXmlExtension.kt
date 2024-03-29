package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.directives.SvelteDirectiveTypes
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil.DIRECTIVE_SEPARATOR
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteXmlExtension : HtmlXmlExtension() {
  override fun isAvailable(file: PsiFile): Boolean = file.language is SvelteHTMLLanguage

  /**
   * Whether writing self-closing `<tag/>` is correct.
   *
   * Svelte compiles it to proper HTML in all cases.
   */
  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
    return true
  }

  /**
   * Whether should warn when writing `<tag></tag>` with empty body, with quick fix that replaces it with `<tag/>`
   */
  override fun isCollapsibleTag(tag: XmlTag): Boolean {
    return isSvelteComponentTag(tag.name) || super.isCollapsibleTag(tag)
  }

  override fun isRequiredAttributeImplicitlyPresent(tag: XmlTag, attrName: String): Boolean {
    for (attribute in tag.attributes) {
      if (attribute.name == attrName && attribute.nameElement.text[0] == '{') {
        return true
      }

      if (attribute.name.startsWith(SvelteDirectiveTypes.BIND.delimitedPrefix) && attribute.localName == attrName) {
        return true
      }
    }
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName)
  }

  override fun createTagNameReference(nameElement: ASTNode, startTagFlag: Boolean): TagNameReference? {
    return if (isSvelteComponentTag(nameElement.text)) {
      SvelteTagNameReference(nameElement, startTagFlag)
    }
    else {
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

    val prefix = if (attributeName.contains(DIRECTIVE_SEPARATOR)) attributeName.split(DIRECTIVE_SEPARATOR).firstOrNull() else null
    if (prefix != null && SvelteDirectiveUtil.directivePrefixes.contains(prefix)) {
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

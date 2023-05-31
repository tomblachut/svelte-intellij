package dev.blachut.svelte.lang.format

import com.intellij.formatting.FormattingDocumentModel
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag

class SvelteHtmlPolicy(settings: CodeStyleSettings, documentModel: FormattingDocumentModel) :
  HtmlPolicy(settings, documentModel) {

  override fun shouldBeWrapped(tag: XmlTag): Boolean {
    if (wrappingTags.contains(tag.name)) {
      return !tag.value.textRange.isEmpty
    }

    return super.shouldBeWrapped(tag)
  }

  companion object {
    val wrappingTags = setOf("script", "style")
  }
}

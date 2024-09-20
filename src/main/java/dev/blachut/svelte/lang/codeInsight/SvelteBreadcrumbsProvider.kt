package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.intellij.xml.breadcrumbs.XmlLanguageBreadcrumbsInfoProvider
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock
import dev.blachut.svelte.lang.psi.hasModuleAttribute

class SvelteBreadcrumbsProvider : BreadcrumbsProvider {
  override fun getLanguages(): Array<Language> = arrayOf(SvelteHTMLLanguage.INSTANCE)

  override fun acceptElement(element: PsiElement): Boolean {
    return element is XmlTag || element is SvelteBlock
  }

  override fun getElementInfo(element: PsiElement): String {
    if (element is SvelteBlock) {
      return element.presentation
    }
    element as XmlTag
    if (element.namespacePrefix == "svelte" || isSvelteComponentTag(element.name)) {
      return element.name
    }
    if (element.name == "script") {
      return if (hasModuleAttribute(element)) "script module" else "script";
    }
    return HtmlUtil.getTagPresentation(element)
  }

  override fun getElementTooltip(element: PsiElement): String {
    if (element is SvelteBlock) {
      return element.startTag.text + "..." + element.endTag?.text
    }
    element as XmlTag
    return XmlLanguageBreadcrumbsInfoProvider.getTooltip(element)
  }

  override fun isShownByDefault(): Boolean = false
}
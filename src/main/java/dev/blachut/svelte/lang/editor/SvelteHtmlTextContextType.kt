package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.HtmlTextContextType
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.blocks.SvelteFragment

class SvelteHtmlTextContextType : TemplateContextType(SvelteBundle.message("svelte.context.html.text")) {
  override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
    val file = templateActionContext.file
    val offset = templateActionContext.startOffset

    val language = PsiUtilCore.getLanguageAtOffset(file, offset)
    if (!isSvelteLanguage(language)) {
      return false
    }
    val element = file.viewProvider.findElementAt(offset, language)
    return element == null || isInSvelteHtmlTextLiveTemplateContext(element)
  }
}

internal fun isSvelteLanguage(language: Language): Boolean {
  return language.isKindOf(SvelteHTMLLanguage.INSTANCE)
}

internal fun isInSvelteHtmlTextLiveTemplateContext(context: PsiElement): Boolean {
  return HtmlTextContextType.isInContext(context) || context.parent is SvelteFragment
}

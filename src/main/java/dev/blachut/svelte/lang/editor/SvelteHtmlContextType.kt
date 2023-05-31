package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.XmlContextType
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteHtmlFileType

class SvelteHtmlContextType : TemplateContextType(SvelteBundle.message("svelte.context.html")) {
  override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
    val file = templateActionContext.file
    val offset = templateActionContext.startOffset
    return isMyLanguage(PsiUtilCore.getLanguageAtOffset(file, offset)) && !XmlContextType.isEmbeddedContent(file, offset)
  }

  override fun createHighlighter(): SyntaxHighlighter? {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteHtmlFileType.INSTANCE, null, null)
  }

  companion object {
    fun isMyLanguage(language: Language): Boolean {
      return language.isKindOf(SvelteHTMLLanguage.INSTANCE)
    }
  }
}

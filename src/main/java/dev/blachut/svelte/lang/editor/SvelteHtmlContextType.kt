package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.XmlContextType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteHtmlFileType

private class SvelteHtmlContextType : TemplateContextType(SvelteBundle.message("svelte.context.html")) {
  override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
    val file = templateActionContext.file
    val offset = templateActionContext.startOffset
    return isSvelteLanguage(PsiUtilCore.getLanguageAtOffset(file, offset)) && !XmlContextType.isEmbeddedContent(file, offset)
  }

  override fun createHighlighter(): SyntaxHighlighter? {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteHtmlFileType, null, null)
  }
}
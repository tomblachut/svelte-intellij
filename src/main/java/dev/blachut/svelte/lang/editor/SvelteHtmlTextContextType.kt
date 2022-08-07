package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.HtmlTextContextType
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.psi.blocks.SvelteFragment

class SvelteHtmlTextContextType : TemplateContextType("Svelte HTML Text") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        val offset = templateActionContext.startOffset

        val language = PsiUtilCore.getLanguageAtOffset(file, offset)
        if (!SvelteHtmlContextType.isMyLanguage(language)) {
            return false
        }
        val element = file.viewProvider.findElementAt(offset, language)
        return element == null || isInContext(element)
    }

    companion object {
        fun isInContext(context: PsiElement): Boolean {
            return HtmlTextContextType.isInContext(context) || context.parent is SvelteFragment
        }
    }
}

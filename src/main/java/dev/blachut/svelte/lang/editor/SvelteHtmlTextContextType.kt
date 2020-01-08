package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.HtmlTextContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.parsing.html.psi.SvelteFragment

class SvelteHtmlTextContextType : TemplateContextType("SVELTE_HTML_TEXT", "Svelte HTML Text", HtmlTextContextType::class.java) {
    override fun isInContext(file: PsiFile, offset: Int): Boolean {
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

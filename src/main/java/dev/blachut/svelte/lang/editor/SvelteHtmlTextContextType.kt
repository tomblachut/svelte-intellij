package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.HtmlTextContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.parsing.html.psi.SvelteFragment
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

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
            var tempContext = context
            if (context.elementType === SvelteTokenTypes.HTML_FRAGMENT) {
                tempContext = context.containingFile.viewProvider.findElementAt(context.textOffset, SvelteHTMLLanguage.INSTANCE)
                    ?: context
            }
            return HtmlTextContextType.isInContext(tempContext) || tempContext.parent is SvelteFragment
        }
    }
}

package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.HtmlContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.XmlContextType
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import dev.blachut.svelte.lang.SvelteFileType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteLanguage

class SvelteHtmlContextType : TemplateContextType("SVELTE_HTML", "Svelte HTML", HtmlContextType::class.java) {
    override fun isInContext(file: PsiFile, offset: Int): Boolean {
        return isMyLanguage(PsiUtilCore.getLanguageAtOffset(file, offset)) && !XmlContextType.isEmbeddedContent(file, offset)
    }

    override fun createHighlighter(): SyntaxHighlighter? {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteFileType.INSTANCE, null, null)
    }

    companion object {
        fun isMyLanguage(language: Language): Boolean {
            return language.isKindOf(SvelteHTMLLanguage.INSTANCE) || language.isKindOf(SvelteLanguage.INSTANCE)
        }
    }
}

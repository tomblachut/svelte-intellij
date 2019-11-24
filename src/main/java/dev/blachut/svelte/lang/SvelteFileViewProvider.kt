package dev.blachut.svelte.lang

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import dev.blachut.svelte.lang.psi.SvelteTemplateElementTypes

class SvelteFileViewProvider(virtualFile: VirtualFile, psiManager: PsiManager, eventSystemEnabled: Boolean) : MultiplePsiFilesPerDocumentFileViewProvider(psiManager, virtualFile, eventSystemEnabled), TemplateLanguageFileViewProvider {
    private val htmlLanguage = SvelteHTMLLanguage.INSTANCE

    override fun getBaseLanguage(): Language {
        return SvelteLanguage.INSTANCE
    }

    override fun getTemplateDataLanguage(): Language {
        return htmlLanguage
    }

    override fun getLanguages(): Set<Language> {
        return setOf(SvelteLanguage.INSTANCE, htmlLanguage)
    }

    override fun cloneInner(virtualFile: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider {
        return SvelteFileViewProvider(virtualFile, manager, false)
    }

    override fun createFile(lang: Language): PsiFile? {
        return when {
            lang === htmlLanguage -> {
                val file = LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this) as PsiFileImpl
                file.contentElementType = SvelteTemplateElementTypes.SVELTE_HTML_TEMPLATE_DATA
                file
            }
            lang === SvelteLanguage.INSTANCE -> LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this)
            else -> null
        }
    }
}

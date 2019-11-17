package dev.blachut.svelte.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import dev.blachut.svelte.lang.icons.SvelteIcons
import javax.swing.Icon

/**
 * Required by XmlElementFactory used to create empty script tags
 */
class SvelteHtmlFileType : LanguageFileType(SvelteHTMLLanguage.INSTANCE) {
    override fun getName(): String {
        return "Svelte HTML"
    }

    override fun getDescription(): String {
        return "Svelte HTML"
    }

    override fun getDefaultExtension(): String {
        return "sveltehtml"
    }

    override fun getIcon(): Icon? {
        return SvelteIcons.FILE
    }

    companion object {
        @JvmField
        val INSTANCE = SvelteHtmlFileType()
    }
}

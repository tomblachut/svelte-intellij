package dev.blachut.svelte.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.SvelteIcons
import javax.swing.Icon

class SvelteHtmlFileType private constructor(): LanguageFileType(SvelteHTMLLanguage.INSTANCE) {
    override fun getName(): String {
        return "Svelte"
    }

    override fun getDescription(): String {
        return "Svelte"
    }

    override fun getDefaultExtension(): String {
        return "svelte"
    }

    override fun getIcon(): Icon? {
        return SvelteIcons.Desaturated
    }

    companion object {
        @JvmField
        val INSTANCE = SvelteHtmlFileType()
    }
}

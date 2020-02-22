package dev.blachut.svelte.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import dev.blachut.svelte.lang.icons.SvelteIcons
import javax.swing.Icon

class SvelteHtmlFileType : LanguageFileType(SvelteHTMLLanguage.INSTANCE) {
    override fun getName(): String {
        return "Svelte"
    }

    override fun getDescription(): String {
        return "Svelte Component"
    }

    override fun getDefaultExtension(): String {
        return "svelte"
    }

    override fun getIcon(): Icon? {
        return SvelteIcons.FILE
    }

    companion object {
        @JvmField
        val INSTANCE = SvelteHtmlFileType()
    }
}

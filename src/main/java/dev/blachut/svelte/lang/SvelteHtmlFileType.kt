package dev.blachut.svelte.lang

import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.openapi.fileTypes.LanguageFileType
import dev.blachut.svelte.lang.icons.SvelteIcons
import javax.swing.Icon

class SvelteHtmlFileType : WebFrameworkHtmlFileType(SvelteHTMLLanguage.INSTANCE, "Svelte", "svelte") {
    companion object {
        @JvmField
        val INSTANCE = SvelteHtmlFileType()
    }
}

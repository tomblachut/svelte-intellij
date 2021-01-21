package dev.blachut.svelte.lang

import com.intellij.javascript.web.lang.html.WebFrameworkHtmlDialect

/**
 * Externally it's name is just "Svelte" because it's the root language of Svelte files
 */
class SvelteHTMLLanguage private constructor() : WebFrameworkHtmlDialect("SvelteHTML") {

    override fun isCaseSensitive(): Boolean = true

    companion object {
        @JvmField
        val INSTANCE = SvelteHTMLLanguage()
    }
}

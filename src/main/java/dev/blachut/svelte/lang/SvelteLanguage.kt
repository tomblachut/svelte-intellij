package dev.blachut.svelte.lang

import com.intellij.lang.Language
import com.intellij.psi.templateLanguages.TemplateLanguage

class SvelteLanguage private constructor() : Language("Svelte"), TemplateLanguage {
    companion object {
        val INSTANCE = SvelteLanguage()
    }
}

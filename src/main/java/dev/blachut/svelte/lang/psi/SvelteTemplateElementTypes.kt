package dev.blachut.svelte.lang.psi

import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteLanguage

import dev.blachut.svelte.lang.psi.SvelteTokenTypes.HTML_FRAGMENT

object SvelteTemplateElementTypes {
    private val SVELTE_FRAGMENT: IElementType = SvelteElementType("SVELTE_FRAGMENT")
    val SVELTE_HTML_TEMPLATE_DATA: IElementType = TemplateDataElementType("SVELTE_HTML_TEMPLATE_DATA", SvelteLanguage.INSTANCE, HTML_FRAGMENT, SVELTE_FRAGMENT)
}

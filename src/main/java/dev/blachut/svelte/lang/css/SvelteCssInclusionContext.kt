package dev.blachut.svelte.lang.css

import com.intellij.psi.PsiElement
import com.intellij.psi.css.resolve.CssInclusionContext
import dev.blachut.svelte.lang.SvelteHTMLLanguage


class SvelteCssInclusionContext : CssInclusionContext() {
    override fun processAllCssFilesOnResolving(context: PsiElement): Boolean =
        context.containingFile?.language is SvelteHTMLLanguage
}

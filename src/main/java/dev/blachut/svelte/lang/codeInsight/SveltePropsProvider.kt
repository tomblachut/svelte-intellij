package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.FileViewProvider
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

object SveltePropsProvider {
    fun getComponentProps(viewProvider: FileViewProvider): List<String>? {
        val psiFile = viewProvider.getPsi(SvelteHTMLLanguage.INSTANCE) ?: return null
        if (psiFile !is SvelteHtmlFile) return null
        val jsElement = getJsEmbeddedContent(psiFile.instanceScript) ?: return null

        val propsVisitor = SveltePropsVisitor()
        jsElement.accept(propsVisitor)

        return propsVisitor.props
    }
}

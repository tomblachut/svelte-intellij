package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.xml.XmlElement
import dev.blachut.svelte.lang.SvelteFileType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteLanguage

/**
 * XmlElement interface is required by com.intellij.lang.javascript.psi.resolve.JSResolveUtil.processSiblingsForElement
 * JSElement could also be used but it breaks ES6PsiUtil.resolveDefaultExport()
 */
class SvelteFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SvelteLanguage.INSTANCE), XmlElement {
    override fun getFileType(): FileType {
        return SvelteFileType.INSTANCE
    }

    override fun toString(): String {
        return "Svelte Component"
    }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        return viewProvider.getPsi(SvelteHTMLLanguage.INSTANCE).processDeclarations(processor, state, lastParent, place)
    }

    override fun processElements(processor: PsiElementProcessor<*>?, place: PsiElement?): Boolean {
        return (viewProvider.getPsi(SvelteHTMLLanguage.INSTANCE) as SvelteHtmlFile).processElements(processor, place)
    }
}

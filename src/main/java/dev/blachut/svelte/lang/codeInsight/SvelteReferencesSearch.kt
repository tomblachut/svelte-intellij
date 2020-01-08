package dev.blachut.svelte.lang.codeInsight


import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import dev.blachut.svelte.lang.SvelteFileViewProvider

class SvelteReferencesSearch : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val element = queryParameters.elementToSearch
        val containingFile = element.containingFile
        val componentName = (element as? JSElement)?.name ?: return

        if (containingFile.viewProvider is SvelteFileViewProvider) {
            if (element is ES6ImportedBinding) {
                queryParameters.optimizer.searchWord(
                    componentName,
                    LocalSearchScope(containingFile),
                    UsageSearchContext.IN_CODE,
                    true,
                    element
                )
            }
            if (element is ES6ImportSpecifierAlias) {
                queryParameters.optimizer.searchWord(
                    componentName,
                    LocalSearchScope(containingFile),
                    UsageSearchContext.IN_CODE,
                    true,
                    element,
                    MyProcessor(element)
                )
            }
        }
        if (element is ES6ExportSpecifierAlias && queryParameters.effectiveSearchScope is LocalSearchScope) {
            val scope = (queryParameters.effectiveSearchScope as LocalSearchScope).scope.firstOrNull() ?: return
            if (scope.containingFile.viewProvider !is SvelteFileViewProvider) return

            queryParameters.optimizer.searchWord(
                componentName,
                LocalSearchScope(scope.containingFile),
                UsageSearchContext.IN_CODE,
                true,
                element,
                MyProcessor(element)
            )
        }
    }

    private class MyProcessor(private val target: ES6ImportExportSpecifierAlias) : RequestResultProcessor(target) {
        override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
            if (!target.isValid) {
                return false
            }

            if (element is XmlTag) {
                if (element.name == target.name) {
                    val references = PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
                    references.forEach { consumer.process(it) }
                }
            }
            return true
        }
    }
}

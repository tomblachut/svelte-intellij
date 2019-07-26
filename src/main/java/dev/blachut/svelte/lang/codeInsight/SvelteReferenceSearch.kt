package dev.blachut.svelte.lang.codeInsight


import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.SingleTargetRequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import dev.blachut.svelte.lang.SvelteFileType

class SvelteReferenceSearch : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean {
        ReadAction.run<RuntimeException> {
            val elementToSearch = queryParameters.elementToSearch
            val containingFile = elementToSearch.containingFile ?: return@run
            if (containingFile.virtualFile.fileType is SvelteFileType) {
                if (elementToSearch is ES6ImportedBinding) {
                    val componentName = (elementToSearch as JSNamedElement).name
                    if (componentName != null) {
                        queryParameters.optimizer.searchWord(
                            componentName,
                            LocalSearchScope(containingFile),
                            UsageSearchContext.IN_CODE,
                            true,
                            elementToSearch,
                            SingleTargetRequestResultProcessor(elementToSearch)
                        )
                    }
                }
                if (elementToSearch is ES6ImportSpecifierAlias) {
                    val componentName = (elementToSearch as ES6ImportExportSpecifierAlias).qualifiedName
                    if (componentName != null) {
                        queryParameters.optimizer.searchWord(
                            componentName,
                            LocalSearchScope(containingFile),
                            UsageSearchContext.IN_CODE,
                            true,
                            elementToSearch,
                            MyProcessor(elementToSearch)
                        )
                    }
                }
            }
            if (elementToSearch is ES6ExportSpecifierAlias && queryParameters.effectiveSearchScope is LocalSearchScope) {
                val qualifiedName = elementToSearch.qualifiedName
                if (qualifiedName != null) {
                    val scope = (queryParameters.effectiveSearchScope as LocalSearchScope).scope.firstOrNull() ?: return@run
                    queryParameters.optimizer.searchWord(
                        qualifiedName,
                        LocalSearchScope(scope.containingFile),
                        UsageSearchContext.IN_CODE,
                        true,
                        elementToSearch,
                        MyProcessor(elementToSearch)
                    )
                }
            }
        }
        return true
    }



    private class MyProcessor(private val target: PsiElement) : RequestResultProcessor(target) {
        override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
            if (!target.isValid) {
                return false
            }

            val alias = target as ES6ImportExportSpecifierAlias

            if (element is XmlTag) {
                if (element.name == alias.name) {
                    val references = PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
                    references.forEach {ref ->
                        consumer.process(ref)
                    }
                }
            }
            return true
        }
    }
}

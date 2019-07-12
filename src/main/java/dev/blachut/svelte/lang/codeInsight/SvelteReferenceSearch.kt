package dev.blachut.svelte.lang.codeInsight


import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SingleTargetRequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import dev.blachut.svelte.lang.SvelteFileType

class SvelteReferenceSearch : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean {
        ReadAction.run<RuntimeException> {
            val elementToSearch = queryParameters.elementToSearch
            val containingFile = elementToSearch.containingFile
            if (elementToSearch is ES6ImportedBinding && containingFile.virtualFile.fileType is SvelteFileType) {
                val componentName = elementToSearch.name
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
        }
        return true
    }
}
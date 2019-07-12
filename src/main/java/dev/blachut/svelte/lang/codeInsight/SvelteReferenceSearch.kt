package dev.blachut.svelte.lang.codeInsight


import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.psi.PsiReference
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor

class SvelteReferenceSearch : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean {
        val elementToSearch = queryParameters.elementToSearch
        if (elementToSearch is ES6ImportedBinding) {
            val componentName = elementToSearch.name ?: return true
            queryParameters.optimizer.searchWord(
                    componentName,
                    LocalSearchScope(elementToSearch.containingFile),
                    UsageSearchContext.IN_CODE,
                    true,
                    elementToSearch,
                    SingleTargetRequestResultProcessor(elementToSearch)
            )
        }
        return true
    }
}
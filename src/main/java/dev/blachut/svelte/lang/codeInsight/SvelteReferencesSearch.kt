package dev.blachut.svelte.lang.codeInsight


import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteReferencesSearch : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val element = queryParameters.elementToSearch
        val componentName = (element as? JSElement)?.name ?: return

        val searchScope = queryParameters.effectiveSearchScope
        if (searchScope is LocalSearchScope) {
            val scopes = searchScope.scope
            for (scope in scopes) {
                if (scope is JSEmbeddedContent) {
                    val containingFile = scope.containingFile
                    if (containingFile is SvelteHtmlFile) {
                        queryParameters.optimizer.searchWord(
                            componentName,
                            LocalSearchScope(containingFile),
                            UsageSearchContext.IN_CODE,
                            true,
                            element
                        )
                    }
                }
            }
        }
    }
}

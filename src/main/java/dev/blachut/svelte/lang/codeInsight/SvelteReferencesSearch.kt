package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor

class SvelteReferencesSearch : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val element = queryParameters.elementToSearch
        val identifier = (element as? JSElement)?.name ?: return
        val effectiveSearchScope = queryParameters.effectiveSearchScope

        // Some JS features limit their scope to <script> tag content, following block expands that to whole file
        if (effectiveSearchScope is LocalSearchScope) {
            effectiveSearchScope.scope.forEach {
                if (it is JSTagEmbeddedContent) {
                    queryParameters.optimizer.searchWord(
                        identifier,
                        LocalSearchScope(it.containingFile),
                        UsageSearchContext.IN_CODE,
                        true,
                        element
                    )
                }
            }
        }
    }
}

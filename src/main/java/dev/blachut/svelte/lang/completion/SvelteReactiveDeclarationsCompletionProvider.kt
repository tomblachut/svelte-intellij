package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.completion.ES6CompletionKeywordsContributor
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSKeywordsCompletionProvider
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.psi.SvelteJSEmbeddedContentImpl

class SvelteReactiveDeclarationsCompletionProvider: CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val parent = parameters.position.parent
        assert(parent is JSReferenceExpression)
        if (skipReferenceCompletionByContext(parameters.position)) return
        val referenceExpression = parent as JSReferenceExpression

        if (referenceExpression.hasQualifier()) return
        if (referenceExpression.parentOfType<JSEmbeddedContent>() is SvelteJSEmbeddedContentImpl) {
            // only complete in Svelte expressions and blocks,
            return
        }

        calcReactiveVariants(referenceExpression, parameters, result)
    }

    // based on JSReferenceCompletionUtil.calcDefaultVariants
    private fun calcReactiveVariants(expression: JSReferenceExpression, parameters: CompletionParameters, resultSet: CompletionResultSet) {
        val parent = expression.parent
        if (JSResolveUtil.isSelfReference(parent, expression)) {
            return  // Prevent Rulezz to appear
        }
        val sink = CompletionResultSink(expression, resultSet.prefixMatcher, emptySet(), !parameters.isExtendedCompletion, false)
        // custom processor is crucial for reactive declaration completions. Would be best to make JS core customisable.
        val localProcessor = SvelteReactiveDeclarationsUtil.SvelteSinkResolveProcessor(sink.name, sink.place!!, sink)
        JSReferenceExpressionImpl.doProcessLocalDeclarations(expression, null, localProcessor, false, true, null)
        val results = sink.resultsAsObjects
        if (results.isNotEmpty()) {
            // results will contain everything in scope, later LookupElement duplicates from JS core get merged
            JSCompletionUtil.pushVariants(results, emptySet(), resultSet)
        }
    }

    // copied JSReferenceCompletionProvider.skipReferenceCompletionByContext because it's package-private
    @Deprecated(message = "Escalate original method and use it")
    private fun skipReferenceCompletionByContext(position: PsiElement): Boolean {
        val parent = position.parent ?: return false
        if (DialectDetector.isActionScript(parent)) return false // for AS, some keywords are provided by reference completion, argh!
        if (ES6CompletionKeywordsContributor.addFromOrAsKeyword(null, parent)) return true
        if (ES6CompletionKeywordsContributor.addExtendsImplements(null, position)) return true
        if (ES6CompletionKeywordsContributor.isAfterExport(parent)) return true
        return if (JSKeywordsCompletionProvider.isExactContextForOperation(parent)) true else false
    }
}

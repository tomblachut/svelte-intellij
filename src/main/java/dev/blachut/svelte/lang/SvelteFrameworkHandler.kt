package dev.blachut.svelte.lang

import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteFrameworkHandler : FrameworkIndexingHandler() {
    override fun addTypeFromResolveResult(
        evaluator: JSTypeEvaluator,
        context: JSEvaluateContext,
        result: PsiElement
    ): Boolean {
        val expression = context.processedExpression
        if (result is JSVariable && expression is SvelteJSReferenceExpression && expression.isSubscribedReference) {
            try {
                val storeType = result.jsType?.asRecordType() ?: return false
                val subscribeMethod =
                    storeType.findPropertySignature("subscribe")?.jsType as? JSFunctionType ?: return false
                val subscriberFunction =
                    subscribeMethod.parameters[0].inferredType?.substitute() as? JSFunctionType ?: return false
                val storeContentType = subscriberFunction.parameters[0].inferredType ?: return false

                evaluator.addType(storeContentType, expression)
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }
}

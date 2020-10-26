package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveProcessor
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.ResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveState
import com.intellij.psi.util.parentOfType

object SvelteReactiveDeclarationsUtil {
    const val REACTIVE_LABEL = "$"

    class SvelteSinkResolveProcessor<T : ResultSink>(name: String?, place: PsiElement, sink: T) :
        SinkResolveProcessor<T>(name, place, sink) {
        override fun executeAcceptedElement(element: PsiElement, state: ResolveState): Boolean {
            val shouldContinue = super.executeAcceptedElement(element, state)

            if (shouldContinue && element is JSDefinitionExpression) {
                val labeledStatement = element.parentOfType<JSLabeledStatement>()
                if (labeledStatement != null && labeledStatement.label == REACTIVE_LABEL) {
                    addPossibleCandidateResult(element, null)
                }
            }

            return true
        }
    }

    class SvelteTypeScriptResolveProcessor<T : ResultSink>(sink: T, containingFile: PsiFile, place: PsiElement) :
        TypeScriptResolveProcessor<ResolveResultSink>(sink, containingFile, place) {
        override fun executeAcceptedElement(element: PsiElement, state: ResolveState): Boolean {
            val shouldContinue = super.executeAcceptedElement(element, state)

            if (shouldContinue && element is JSDefinitionExpression) {
                val labeledStatement = element.parentOfType<JSLabeledStatement>()
                if (labeledStatement != null && labeledStatement.label == REACTIVE_LABEL) {
                    addPossibleCandidateResult(element, null)
                }
            }

            return true
        }
    }
}

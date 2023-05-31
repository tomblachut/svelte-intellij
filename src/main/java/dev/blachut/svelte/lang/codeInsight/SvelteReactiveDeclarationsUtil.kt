package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveProcessor
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.ResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.ResolveState
import com.intellij.psi.util.parentOfType

object SvelteReactiveDeclarationsUtil {
  const val REACTIVE_LABEL = "$"

  fun processLocalDeclarations(place: PsiElement, referenceName: String, incompleteCode: Boolean): Array<ResolveResult> {
    val sink = ResolveResultSink(place, referenceName, false, incompleteCode)
    val processor = SvelteSinkResolveProcessor(referenceName, place, sink)
    // below could be used because it seems to be more widely used in JavaScript
    //  JSReferenceExpressionImpl.doProcessLocalDeclarations(expression, qualifier, localProcessor, false, false, null);
    JSResolveUtil.treeWalkUp(processor, place, place, place, place.containingFile)

    return processor.resultsAsResolveResults
  }

  class SvelteSinkResolveProcessor<T : ResultSink>(name: String?, place: PsiElement, sink: T) :
    SinkResolveProcessor<T>(name, place, sink) {
    override fun executeAcceptedElement(element: PsiElement, state: ResolveState): Boolean {
      return super.executeAcceptedElement(element, state) && executeAcceptedReactiveDeclaration(element, state)
    }
  }

  class SvelteTypeScriptResolveProcessor<T : ResultSink>(sink: T, containingFile: PsiFile, place: PsiElement) :
    TypeScriptResolveProcessor<ResolveResultSink>(sink, containingFile, place) {
    override fun executeAcceptedElement(element: PsiElement, state: ResolveState): Boolean {
      return super.executeAcceptedElement(element, state) && executeAcceptedReactiveDeclaration(element, state)
    }
  }

  fun <T : ResultSink> SinkResolveProcessor<T>.executeAcceptedReactiveDeclaration(element: PsiElement, state: ResolveState): Boolean {
    if (element is JSDefinitionExpression) {
      val labeledStatement = element.parentOfType<JSLabeledStatement>()
      if (labeledStatement != null && labeledStatement.label == REACTIVE_LABEL) {
        return resultSink.addResult(element, state, placeTopParent)
      }
    }

    return true
  }
}

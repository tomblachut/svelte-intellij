package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import dev.blachut.svelte.lang.parsing.js.isSingleDollarPrefixedName
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache

abstract class SvelteInnerReferenceExpressionResolver(
  private val myRef: JSReferenceExpressionImpl,
  private val myReferencedName: String?,
  private val myQualifier: JSExpression?,
) : ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    val resolvedImplicits = resolveImplicits(expression)
    if (resolvedImplicits.isNotEmpty()) return resolvedImplicits

    val resolvedStore = resolveLocalStore(expression, incompleteCode)
    if (resolvedStore.isNotEmpty()) return resolvedStore

    // sometimes reactive declaration could've been already returned here, the proof was lost to time
    val resolvedBasic = resolveBasic(expression, incompleteCode)
    if (resolvedBasic.isNotEmpty()) return resolvedBasic

    val resolvedReactiveDeclaration = resolveReactiveDeclarations(expression, incompleteCode)
    if (resolvedReactiveDeclaration.isNotEmpty()) return resolvedReactiveDeclaration

    return ResolveResult.EMPTY_ARRAY
  }

  /**
   * Finds locally defined and imported stores, skips auto-imports, since these can be shadowed by globally available runes.
   */
  private fun resolveLocalStore(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    if (isSingleDollarPrefixedName(myReferencedName) && myQualifier == null) {
      val storeName = removeSingleDollarPrefixUnchecked(myReferencedName!!)

      val processor = JSDialectSpecificHandlersFactory.forElement(expression).createResolveProcessor(storeName, expression, incompleteCode)
      JSReferenceExpressionImpl.doProcessLocalDeclarations(expression, null, processor, false, false, null)

      return processor.resultsAsResolveResults
    }

    return ResolveResult.EMPTY_ARRAY
  }

  protected abstract fun resolveBasic(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult>

  private fun resolveReactiveDeclarations(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    val referencedName = myReferencedName
    if (expression.qualifier != null || referencedName == null) return ResolveResult.EMPTY_ARRAY

    val sink = ResolveResultSink(myRef, referencedName, false, incompleteCode)
    val localProcessor = createReactiveDeclarationsProcessor(sink)
    return SvelteReactiveDeclarationsUtil.resolveReactiveDeclarationsCommon(myRef, myQualifier, localProcessor)
  }

  protected abstract fun createReactiveDeclarationsProcessor(sink: ResolveResultSink): SinkResolveProcessor<ResolveResultSink>
}

private val implicitIdentifiers = arrayOf("\$\$props", "\$\$restProps", "\$\$slots")

internal fun resolveImplicits(expression: JSReferenceExpression): Array<ResolveResult> {
  implicitIdentifiers.forEach {
    if (JSSymbolUtil.isAccurateReferenceExpressionName(expression, it)) {
      val element = JSImplicitElementImpl.Builder(it, expression)
        .forbidAstAccess()
        .setType(JSImplicitElement.Type.Variable)
        .setProperties(JSImplicitElement.Property.Constant)
        .toImplicitElement()
      return arrayOf(JSResolveResult(element))
    }
  }
  return emptyArray()
}

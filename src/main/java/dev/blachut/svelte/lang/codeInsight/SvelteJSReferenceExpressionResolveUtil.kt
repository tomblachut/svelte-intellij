package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
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

    // sometimes reactive declaration could've been already returned here, the proof was lost to time
    val resolvedBasicOrStore = resolveBasic(expression, incompleteCode)
    if (resolvedBasicOrStore.isNotEmpty()) return resolvedBasicOrStore

    val resolvedReactiveDeclaration = resolveReactiveDeclarations(expression, incompleteCode)
    if (resolvedReactiveDeclaration.isNotEmpty()) return resolvedReactiveDeclaration

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

internal fun isSingleDollarPrefixedName(name: String): Boolean {
  return name.length > 1 && name[0] == '$' && name[1] != '$'
}
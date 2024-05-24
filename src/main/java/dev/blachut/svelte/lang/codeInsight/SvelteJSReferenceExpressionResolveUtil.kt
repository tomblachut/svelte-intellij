package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.ResolveResult

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

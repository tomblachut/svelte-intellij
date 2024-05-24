// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.ResolveResult

class SvelteJSReferenceExpressionResolver(
  referenceExpression: JSReferenceExpressionImpl,
  ignorePerformanceLimits: Boolean,
) : JSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {
  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    val resolvedImplicits = resolveImplicits(expression)
    if (resolvedImplicits.isNotEmpty()) return resolvedImplicits

    // sometimes reactive declaration could've been already returned here, the proof was lost to time
    val resolvedBasicOrStore = super.resolve(expression, incompleteCode)
    if (resolvedBasicOrStore.isNotEmpty()) return resolvedBasicOrStore

    val referencedName = myReferencedName
    if (expression.qualifier != null || referencedName == null) return ResolveResult.EMPTY_ARRAY
    val sink = ResolveResultSink(myRef, referencedName, false, incompleteCode)
    val localProcessor = createLocalResolveProcessor(sink)
    return SvelteReactiveDeclarationsUtil.resolveReactiveDeclarationsCommon(myRef, myQualifier, localProcessor)
  }

  private fun createLocalResolveProcessor(sink: ResolveResultSink): SinkResolveProcessor<ResolveResultSink> {
    return SvelteReactiveDeclarationsUtil.SvelteSinkResolveProcessor(myReferencedName, myRef, sink)
  }
}

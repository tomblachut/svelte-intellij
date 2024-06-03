// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.ResolveResult

class SvelteJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl, ignorePerformanceLimits: Boolean)
  : JSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {
  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    return inner.resolve(expression, incompleteCode)
  }

  private val inner = object : SvelteInnerReferenceExpressionResolver(myRef, myReferencedName, myQualifier) {
    override fun resolveBasic(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
      return super@SvelteJSReferenceExpressionResolver.resolve(expression, incompleteCode)
    }

    override fun createReactiveDeclarationsProcessor(sink: ResolveResultSink): SinkResolveProcessor<ResolveResultSink> {
      return SvelteReactiveDeclarationsUtil.SvelteSinkResolveProcessor(myReferencedName, myRef, sink)
    }
  }
}

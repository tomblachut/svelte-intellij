// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.codeInsight.SvelteJSReferenceExpressionResolver.Companion.resolveImplicits

class SvelteTypeScriptReferenceExpressionResolver(
    referenceExpression: JSReferenceExpressionImpl,
    ignorePerformanceLimits: Boolean
) : TypeScriptReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {
    override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
        val resolveImplicits = resolveImplicits(expression)
        if (resolveImplicits.isNotEmpty()) return resolveImplicits

        val resolved = super.resolve(expression, incompleteCode)

        if (resolved.isEmpty() && expression.qualifier == null && myReferencedName != null) {
            val sink = ResolveResultSink(myRef, myReferencedName, false, incompleteCode)
            val localProcessor = createLocalResolveProcessor(sink)
            JSReferenceExpressionImpl.doProcessLocalDeclarations(
                myRef,
                myQualifier,
                localProcessor,
                false,
                false,
                null
            )
            val jsElement = localProcessor.result ?: return resolved

            val labeledStatement = jsElement.parentOfType<JSLabeledStatement>()
            if (labeledStatement != null && labeledStatement.label == SvelteReactiveDeclarationsUtil.REACTIVE_LABEL) {
                return localProcessor.resultsAsResolveResults
            }
        }

        return resolved
    }

    override fun createLocalResolveProcessor(sink: ResolveResultSink): SinkResolveProcessor<ResolveResultSink> {
        return SvelteReactiveDeclarationsUtil.SvelteTypeScriptResolveProcessor(sink, myContainingFile, myRef)
    }
}

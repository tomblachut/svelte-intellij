// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.psi.*

class SvelteJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                          ignorePerformanceLimits: Boolean) :
    JSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {
    override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
        val propsReferenceName = "\$\$props"

        val name = expression.referenceName
        if (name != null && expression.qualifier == null && name.length > 1 && name[0] == '$' && name[1] != '$') {
            val element = JSImplicitElementImpl.Builder(name, expression)
                .forbidAstAccess()
                .setProperties(JSImplicitElement.Property.Constant)
                .toImplicitElement()
            return arrayOf(JSResolveResult(element))
        }

        if (JSSymbolUtil.isAccurateReferenceExpressionName(expression, propsReferenceName)) {
            val element = JSImplicitElementImpl.Builder(propsReferenceName, expression)
                .forbidAstAccess()
                .setProperties(JSImplicitElement.Property.Constant)
                .toImplicitElement()
            return arrayOf(JSResolveResult(element))
        }

        return resolveInComponent(expression) ?: super.resolve(expression, incompleteCode)
    }

    private fun resolveInComponent(expression: JSReferenceExpressionImpl): Array<ResolveResult>? {
        if (expression.qualifier != null) return null

        return resolveInLocalBlock() ?: resolveInSvelteBlocks()
    }

    private fun resolveInLocalBlock(): Array<ResolveResult>? {
        val keyExpression = PsiTreeUtil.getParentOfType(myRef, SvelteKeyExpression::class.java) ?: return null
        val block = keyExpression.parent as SvelteEachBlockOpeningTag
        block.parameterList.forEach { parameter ->
            val result = resolveInSvelteParameter(parameter)
            if (result != null) return result
        }
        return null
    }

    private fun resolveInSvelteBlocks(): Array<ResolveResult>? {
        var currentElement: PsiElement? = myRef

        if (currentElement != null && myContainingFile.language == SvelteHTMLLanguage.INSTANCE) {
            // Jump between PSI trees
            currentElement = myContainingFile.viewProvider.findElementAt(currentElement.textOffset, SvelteLanguage.INSTANCE)
        }

        while (currentElement != null) {
            val scope = PsiTreeUtil.getParentOfType(currentElement, SvelteScope::class.java)
            if (scope != null) {
                val scopeResults = resolveInSvelteBlock(scope)
                if (scopeResults != null) return scopeResults
            }
            currentElement = scope
        }

        return null
    }

    private fun resolveInSvelteBlock(scope: SvelteScope): Array<ResolveResult>? {
        when (val block = scope.parent) {
            is SvelteEachBlockOpening -> {
                block.eachBlockOpeningTag.parameterList.forEach { parameter ->
                    val result = resolveInSvelteParameter(parameter)
                    if (result != null) return result
                }
            }
            is SvelteAwaitThenBlockOpening -> {
                val parameter = block.awaitThenBlockOpeningTag.parameter ?: return null
                return resolveInSvelteParameter(parameter)
            }
            is SvelteThenContinuation -> {
                val parameter = block.thenContinuationTag.parameter ?: return null
                return resolveInSvelteParameter(parameter)
            }
            is SvelteCatchContinuation -> {
                val parameter = block.catchContinuationTag.parameter ?: return null
                return resolveInSvelteParameter(parameter)
            }
        }
        return null
    }

    private fun resolveInSvelteParameter(parameter: SvelteParameter): Array<ResolveResult>? {
        val variables = PsiTreeUtil.findChildrenOfType(parameter, SvelteJSParameter::class.java)
        variables.forEach { if (it.name == myReferencedName) return arrayOf(JSResolveResult(it)) }
        return null
    }
}

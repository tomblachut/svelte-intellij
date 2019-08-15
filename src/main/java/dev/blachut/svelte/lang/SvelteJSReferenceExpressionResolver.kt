// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.psi.*

class SvelteJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                          ignorePerformanceLimits: Boolean) :
    JSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {
    override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
        return if (myUnqualifiedOrLocalResolve) {
            resolveInComponent(expression, incompleteCode) ?: arrayOf()
        } else {
            super.resolve(expression, incompleteCode)
        }
    }

    private fun resolveInComponent(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult>? {
        if (expression.qualifier != null) return null

        val injectedLanguageManager = InjectedLanguageManager.getInstance(expression.project)
        val host = injectedLanguageManager.getInjectionHost(expression) ?: return null

        return resolveInSvelteBlocks(host, injectedLanguageManager)
            ?: resolveInScriptTag(host.containingFile, incompleteCode)
    }

    private fun resolveInSvelteBlocks(host: PsiLanguageInjectionHost, injectedLanguageManager: InjectedLanguageManager): Array<ResolveResult>? {
        var scopeResults: Array<ResolveResult>? = null
        var currentElement: PsiElement? = host
        while (scopeResults == null && currentElement != null) {
            // TODO Support each block key expression
            val scope = PsiTreeUtil.getParentOfType(currentElement, SvelteScope::class.java)
            if (scope != null) {
                scopeResults = resolveInSvelteBlock(injectedLanguageManager, scope)
            }
            currentElement = scope
        }


        return scopeResults
    }

    private fun resolveInSvelteBlock(injectedLanguageManager: InjectedLanguageManager, scope: SvelteScope): Array<ResolveResult>? {
        when (val block = scope.parent) {
            is SvelteEachBlockOpening -> {
                block.eachBlockOpeningTag.parameterList.forEach { parameter ->
                    val result = resolveInSvelteParameter(injectedLanguageManager, parameter)
                    if (result != null) return result
                }
            }
            is SvelteAwaitThenBlockOpening -> {
                val parameter = block.awaitThenBlockOpeningTag.parameter ?: return null
                return resolveInSvelteParameter(injectedLanguageManager, parameter)
            }
            is SvelteThenContinuation -> {
                val parameter = block.thenContinuationTag.parameter ?: return null
                return resolveInSvelteParameter(injectedLanguageManager, parameter)
            }
            is SvelteCatchContinuation -> {
                val parameter = block.catchContinuationTag.parameter ?: return null
                return resolveInSvelteParameter(injectedLanguageManager, parameter)
            }
        }
        return null
    }

    private fun resolveInSvelteParameter(injectedLanguageManager: InjectedLanguageManager, parameter: SvelteParameter): Array<ResolveResult>? {
        val injectedFile = injectedLanguageManager.getInjectedPsiFiles(parameter)?.first()?.first ?: return null

        val variables = PsiTreeUtil.findChildrenOfType(injectedFile, JSVariable::class.java)
        variables.forEach { if (it.name == myReferencedName) return arrayOf(JSResolveResult(it)) }
        return null
    }

    private fun resolveInScriptTag(svelteFile: PsiFile, incompleteCode: Boolean): Array<ResolveResult>? {
        val scriptTag = findScriptTag(svelteFile.viewProvider.getPsi(HTMLLanguage.INSTANCE)) ?: return null
        val jsRoot = PsiTreeUtil.getChildOfType(scriptTag, JSEmbeddedContent::class.java) ?: return null

        val sink = ResolveResultSink(myRef, this.myReferencedName!!, false, incompleteCode)
        val localProcessor = createLocalResolveProcessor(sink)
        localProcessor.isToProcessHierarchy = true

        JSReferenceExpressionImpl.doProcessLocalDeclarations(jsRoot, this.myQualifier, localProcessor, false, false, null as Boolean?)
        val jsElement = localProcessor.result
        if (jsElement != null) {
            return localProcessor.resultsAsResolveResults
        }

        return null
    }

    private fun findScriptTag(file: PsiFile): XmlTag? {
        return PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java).find { HtmlUtil.isScriptTag(it) }
    }
}

package dev.blachut.svelte.lang

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.ResolveCache
import dev.blachut.svelte.lang.codeInsight.SvelteStubBasedScopeHandler
import dev.blachut.svelte.lang.codeInsight.SvelteTypeScriptReferenceExpressionResolver

class SvelteTypeScriptSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(
    referenceExpression: JSReferenceExpressionImpl,
    ignorePerformanceLimits: Boolean
  ): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return SvelteTypeScriptReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun getStubBasedScopeHandler(): JSStubBasedScopeHandler {
    return SvelteStubBasedScopeHandler
  }

  override fun getExportScope(element: PsiElement): JSElement? {
    return getSvelteExportScope(element)
  }
}

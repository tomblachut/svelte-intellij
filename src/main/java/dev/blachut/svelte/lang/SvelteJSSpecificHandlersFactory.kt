package dev.blachut.svelte.lang

import com.intellij.lang.ecmascript6.ES6SpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.ResolveCache
import dev.blachut.svelte.lang.codeInsight.SvelteJSReferenceExpressionResolver
import dev.blachut.svelte.lang.codeInsight.SvelteStubBasedScopeHandler
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSEmbeddedContentImpl
import dev.blachut.svelte.lang.psi.findAncestorScript
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

class SvelteJSSpecificHandlersFactory : ES6SpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(
    referenceExpression: JSReferenceExpressionImpl,
    ignorePerformanceLimits: Boolean
  ): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return SvelteJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun getStubBasedScopeHandler(): JSStubBasedScopeHandler {
    return SvelteStubBasedScopeHandler
  }

  override fun getExportScope(element: PsiElement): JSElement? {
    return getSvelteExportScope(element)
  }
}

fun getSvelteExportScope(element: PsiElement): JSElement? {
  if (element is PsiFile || element is SvelteJSEmbeddedContentImpl)
    return null
  val svelteFile = element.containingFile as? SvelteHtmlFile
                   ?: return null
  val script = findAncestorScript(element)
               ?: svelteFile.instanceScript
               ?: svelteFile.moduleScript
  return script?.let { getJsEmbeddedContent(it) }
}
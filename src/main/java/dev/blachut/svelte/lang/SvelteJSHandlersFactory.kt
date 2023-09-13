package dev.blachut.svelte.lang

import com.intellij.lang.ecmascript6.ES6HandlersFactory
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import dev.blachut.svelte.lang.codeInsight.SvelteAddImportExecutor
import dev.blachut.svelte.lang.codeInsight.SvelteImportPlaceInfo
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

fun createSvelteImportFactories(): List<JSImportExecutorFactory> {
  return listOf(object : JSImportExecutorFactory {
    override fun createExecutor(place: PsiElement): JSAddImportExecutor {
      return SvelteAddImportExecutor(place)
    }
  })
}

class SvelteHtmlHandlersFactory : ES6HandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return createSvelteImportFactories()
  }

  /**
   * Unique to [SvelteHtmlHandlersFactory] because we need to redirect language of [SvelteHtmlTag] to [JSLanguageDialect]
   */
  override fun createImportPlaceInfo(place: PsiElement, results: Array<ResolveResult>): JSImportPlaceInfo {
    return SvelteImportPlaceInfo(place, results)
  }
}

class SvelteJSHandlersFactory : ES6HandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return createSvelteImportFactories()
  }
}

class SvelteTSHandlersFactory : TypeScriptHandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return createSvelteImportFactories()
  }
}

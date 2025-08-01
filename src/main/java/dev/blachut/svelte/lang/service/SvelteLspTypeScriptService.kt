// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.JSFrameworkLspAnnotationErrorFilter
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.lang.typescript.lsp.LspAnnotationErrorFilter
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.eclipse.lsp4j.Diagnostic

/**
 * @see SvelteLspServerSupportProvider
 * @see SvelteLspServerDescriptor
 */
class SvelteLspTypeScriptService(project: Project)
  : JSFrameworkLspTypeScriptService(project, SvelteLspServerSupportProvider::class.java, SvelteLspServerActivationRule) {
  override val name = "Svelte LSP"
  override val prefix = "Svelte"

  override fun isServiceFallbackResolveEnabled(): Boolean  = true

  override fun isServiceNavigationEnabled(): Boolean = true

  override fun getCompletionMergeStrategy(
    parameters: CompletionParameters,
    file: PsiFile,
    context: PsiElement,
  ): TypeScriptService.CompletionMergeStrategy {
    if (context.parent is SvelteHtmlAttribute) {
      return TypeScriptService.CompletionMergeStrategy.MERGE
    }

    // todo (?) handle components with auto imports

    val isJavaScript = false // this might be too strict
    return TypeScriptLanguageServiceUtil.getMergeStrategyForPosition(context, isJavaScript)
  }

  override fun createAnnotationErrorFilter(): LspAnnotationErrorFilter = SvelteLspAnnotationErrorFilter(project)
}

private class SvelteLspAnnotationErrorFilter(project: Project) : JSFrameworkLspAnnotationErrorFilter(project) {
  private val showA11yWarnings = getSvelteServiceSettings(project).showA11yWarnings

  override fun isProblemEnabled(diagnostic: Diagnostic): Boolean {
    if (!showA11yWarnings && diagnostic.code?.left?.startsWith("a11y") == true) return false
    return super.isProblemEnabled(diagnostic)
  }
}

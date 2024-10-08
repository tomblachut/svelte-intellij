// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

/**
 * @see SvelteLspServerSupportProvider
 * @see SvelteLspServerDescriptor
 */
class SvelteLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, SvelteLspServerSupportProvider::class.java) {
  override val name = "Svelte LSP"
  override val prefix = "Svelte"

  override fun isAcceptable(file: VirtualFile) = SvelteServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)

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
}

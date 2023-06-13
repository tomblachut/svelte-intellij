// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.psi.PsiFile

class SvelteLspTypeScriptService(project: Project) : JSFrameworkLspTypeScriptService(project) {
  override fun getProviderClass(): Class<out LspServerSupportProvider> = SvelteLspServerSupportProvider::class.java

  override val name = "Svelte LSP"
  override val prefix = "Svelte"
  override val serverVersion = svelteLanguageToolsVersion

  override fun createQuickInfoResponse(rawResponse: String): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      displayString = rawResponse.removeSurrounding("<html><body><pre>", "</pre></body></html>")
    }
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isFileAcceptableForService(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isServiceEnabledAndAvailable(project, file)

  override fun isServiceEnabledBySettings(project: Project): Boolean {
    return isSvelteServiceEnabledBySettings(project)
  }

  override fun getLspServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
    return getSvelteServerDescriptor(project, file)
  }
}

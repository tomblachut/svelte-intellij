// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.util.getOffsetInDocument
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

/**
 * @see SvelteLspServerSupportProvider
 * @see SvelteLspServerDescriptor
 */
class SvelteLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, SvelteLspServerSupportProvider::class.java) {
  override val name = "Svelte LSP"
  override val prefix = "Svelte"

  override fun isAcceptable(file: VirtualFile) = SvelteServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)

  override fun getNavigationFor(document: Document, sourceElement: PsiElement): Array<PsiElement> {
    val server = getServer() ?: return emptyArray()
    val file = FileDocumentManager.getInstance().getFile(document) ?: return emptyArray()
    val raw = server.requestExecutor.getElementDefinitions(file, sourceElement.textOffset)

    return raw.mapNotNull { locationLink ->
      val targetFile = server.descriptor.findFileByUri(locationLink.targetUri) ?: return@mapNotNull null
      val targetPsiFile = PsiManager.getInstance(project).findFile(targetFile) ?: return@mapNotNull null
      val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetPsiFile) ?: return@mapNotNull null
      val offset = getOffsetInDocument(targetDocument, locationLink.targetSelectionRange.start)
      if (offset != null) {
        val leaf = targetPsiFile.findElementAt(offset)
        if (leaf == sourceElement) return@mapNotNull null // discard self referencing LocationLinks, otherwise GTDU is confused
        JSDocumentationUtils.getOriginalElementOrParentIfLeaf(leaf)
      }
      else {
        targetPsiFile
      }
    }.toTypedArray()
  }

  override fun getCompletionMergeStrategy(parameters: CompletionParameters,
                                          file: PsiFile,
                                          context: PsiElement): TypeScriptService.CompletionMergeStrategy {
    if (context.parent is SvelteHtmlAttribute) {
      return TypeScriptService.CompletionMergeStrategy.MERGE
    }

    // todo (?) handle components with auto imports

    val isJavaScript = false // this might be too strict
    return TypeScriptLanguageServiceUtil.getMergeStrategyForPosition(context, isJavaScript)
  }
}

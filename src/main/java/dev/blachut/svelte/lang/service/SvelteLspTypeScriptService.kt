// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.javascript.documentation.JSQuickNavigateBuilder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.util.convertMarkupContentToHtml
import com.intellij.platform.lsp.util.getOffsetInDocument
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import org.eclipse.lsp4j.MarkupContent

/**
 * @see SvelteLspServerSupportProvider
 * @see SvelteLspServerDescriptor
 */
class SvelteLspTypeScriptService(project: Project) : JSFrameworkLspTypeScriptService(project) {
  override fun getProviderClass(): Class<out LspServerSupportProvider> = SvelteLspServerSupportProvider::class.java

  override val name = "Svelte LSP"
  override val prefix = "Svelte"
  override val serverVersion = svelteLanguageToolsVersion

  override fun createQuickInfoResponse(markupContent: MarkupContent): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      val content = HtmlBuilder().appendRaw(convertMarkupContentToHtml(markupContent)).toString()
      val parts = content.split("<hr />")

      displayString = parts[0]
        .removeSurrounding("<pre><code class=\"language-typescript\">", "</code></pre>")
        .trim()
        .let(StringUtil::unescapeXmlEntities)
      if (parts.size == 2) {
        documentation = parts[1]
      }
      // Svelte LS omits "export" so we can't assign kindModifiers
    }
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement): Array<PsiElement> {
    return withServer {
      val file = FileDocumentManager.getInstance().getFile(document) ?: return emptyArray()
      val raw = requestExecutor.getElementDefinitions(file, sourceElement.textOffset)

      raw.mapNotNull { locationLink ->
        val targetFile = descriptor.findFileByUri(locationLink.targetUri) ?: return@mapNotNull null
        val targetPsiFile = PsiManager.getInstance(project).findFile(targetFile) ?: return@mapNotNull null
        val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetPsiFile) ?: return@mapNotNull null
        val offset = getOffsetInDocument(targetDocument, locationLink.targetSelectionRange.start)
        if (offset != null) {
          val leaf = targetPsiFile.findElementAt(offset)
          JSQuickNavigateBuilder.getOriginalElementOrParentIfLeaf(leaf)
        }
        else {
          targetPsiFile
        }
      }.toTypedArray()
    } ?: emptyArray()
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isFileAcceptableForService(file.virtualFile ?: return false)
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

  override fun isAcceptable(file: VirtualFile) = isServiceEnabledAndAvailable(project, file)

  override fun isServiceEnabledBySettings(project: Project): Boolean {
    return isSvelteServiceEnabledBySettings(project)
  }

  override fun getLspServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
    return getSvelteServerDescriptor(project, file)
  }
}

package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.ProjectManager
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.impl.LspDidChangeUtil
import org.eclipse.lsp4j.TextDocumentContentChangeEvent

/**
 * All symbols in this file are equivalent of function
 * [addDidChangeTextDocumentListener](https://github.com/sveltejs/language-tools/blob/master/packages/svelte-vscode/src/extension.ts#L311)
 * from the svelte-vscode extension.
 *
 * We need to register a custom document listener that sends the custom notification when a change in JS or TS file occurs,
 * because Svelte Language Server doesn't currently support opening those files (JS & TS are supported via TSService).
 */

internal data class SvelteLspDidChangeTsOrJsFileParams(val uri: String, val changes: List<TextDocumentContentChangeEvent>)

private val workaroundFileTypes = setOf(JavaScriptFileType.INSTANCE, TypeScriptFileType.INSTANCE)

internal class SvelteLspCustomDocumentListener : DocumentListener {
  override fun beforeDocumentChange(event: DocumentEvent) {
    val virtualFile = LspDidChangeUtil.getFileToHandle(event) ?: return
    if (virtualFile.fileType !in workaroundFileTypes) return

    for (project in ProjectManager.getInstance().getOpenProjects()) {
      for (lspServer in LspServerManager.getInstance(project).getServersForProvider(SvelteLspServerSupportProvider::class.java)) {
        val didChangeParams = LspDidChangeUtil.createIncrementalDidChangeParamsBeforeDocumentChange(lspServer, event, virtualFile)
        val params = SvelteLspDidChangeTsOrJsFileParams(didChangeParams.textDocument.uri, didChangeParams.contentChanges)
        lspServer.sendNotification { (it as SvelteLsp4jServer).didChangeTsOrJsFile(params) }
      }
    }
  }
}

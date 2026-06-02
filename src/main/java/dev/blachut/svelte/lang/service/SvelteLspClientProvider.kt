// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspClientProvider
import com.intellij.lang.typescript.lsp.JSLspClientWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import dev.blachut.svelte.lang.service.settings.SvelteServiceConfigurable
import icons.SvelteIcons
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.services.LanguageServer

/**
 * @see SvelteLspTypeScriptService
 */
internal class SvelteLspClientProvider : JSFrameworkLspClientProvider(SvelteLspServerActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspClientDescriptor = SvelteLspClientDescriptor(project)

  override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?): LspClientWidgetItem =
    JSLspClientWidgetItem(lspClient, currentFile, SvelteIcons.Original, SvelteIcons.Desaturated, SvelteServiceConfigurable::class.java)
}

/**
 * @see SvelteLspTypeScriptService
 */
internal class SvelteLspClientDescriptor(project: Project) :
  JSFrameworkLspClientDescriptor(project, SvelteLspServerActivationRule, "Svelte") {
  override val lsp4jServerClass: Class<out LanguageServer> = SvelteLsp4jServer::class.java
}

internal interface SvelteLsp4jServer : JSFrameworkLsp4jServer {
  @JsonNotification("\$/onDidChangeTsOrJsFile")
  fun didChangeTsOrJsFile(params: SvelteLspDidChangeTsOrJsFileParams)
}

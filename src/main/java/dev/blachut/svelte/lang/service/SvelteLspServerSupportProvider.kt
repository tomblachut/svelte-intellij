// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerSupportProvider
import com.intellij.lang.typescript.lsp.JSLspServerWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import dev.blachut.svelte.lang.service.settings.SvelteServiceConfigurable
import icons.SvelteIcons
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.services.LanguageServer


/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerSupportProvider : JSFrameworkLspServerSupportProvider(SvelteLspServerActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspServerDescriptor = SvelteLspServerDescriptor(project)

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    JSLspServerWidgetItem(lspServer, currentFile, SvelteIcons.Original, SvelteIcons.Desaturated, SvelteServiceConfigurable::class.java)
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, SvelteLspServerActivationRule, "Svelte") {
  override val lsp4jServerClass: Class<out LanguageServer> = SvelteLsp4jServer::class.java
}

internal interface SvelteLsp4jServer : JSFrameworkLsp4jServer {
  @JsonNotification("\$/onDidChangeTsOrJsFile")
  fun didChangeTsOrJsFile(params: SvelteLspDidChangeTsOrJsFileParams)
}

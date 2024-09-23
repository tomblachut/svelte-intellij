// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import dev.blachut.svelte.lang.service.settings.SvelteServiceConfigurable
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import icons.SvelteIcons
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.services.LanguageServer
import org.jetbrains.annotations.ApiStatus

private object SvelteLspServerPackageDescriptor : LspServerPackageDescriptor("svelte-language-server",
                                                                             "0.17.0",
                                                                             "/bin/server.js") {
  override val defaultVersion: String get() = Registry.stringValue("svelte.language.server.default.version")
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (SvelteServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(SvelteLspServerDescriptor(project))
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    JSLspServerWidgetItem(lspServer, currentFile, SvelteIcons.Original, SvelteIcons.Desaturated, SvelteServiceConfigurable::class.java)
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, SvelteServiceSetActivationRule, "Svelte") {
  override val lsp4jServerClass: Class<out LanguageServer> = SvelteLsp4jServer::class.java
}

internal interface SvelteLsp4jServer : JSFrameworkLsp4jServer {
  @JsonNotification("\$/onDidChangeTsOrJsFile")
  fun didChangeTsOrJsFile(params: SvelteLspDidChangeTsOrJsFileParams)
}

@ApiStatus.Experimental
object SvelteLspExecutableDownloader : LspServerDownloader(SvelteLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).lspServerPackageRef
  }
}

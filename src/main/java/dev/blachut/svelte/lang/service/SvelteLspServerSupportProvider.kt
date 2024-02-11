// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsWidget.LanguageServiceWidgetContext
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

private val svelteLspServerPackageDescriptor: () -> LspServerPackageDescriptor = {
  LspServerPackageDescriptor("svelte-language-server",
                             Registry.stringValue("svelte.language.server.default.version"),
                             "/bin/server.js")
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isServiceEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(SvelteLspServerDescriptor(project))
    }
  }

  override fun getLspServerWidgetItem(context: LanguageServiceWidgetContext, lspServer: LspServer): LspServerWidgetItem =
    LspServerWidgetItem(context, lspServer, SvelteIcons.Original, SvelteServiceConfigurable::class.java)
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, SvelteLspExecutableDownloader, "Svelte") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForService(file)

  override val lsp4jServerClass: Class<out LanguageServer> = SvelteLsp4jServer::class.java
}

internal interface SvelteLsp4jServer : JSFrameworkLsp4jServer {
  @JsonNotification("\$/onDidChangeTsOrJsFile")
  fun didChangeTsOrJsFile(params: SvelteLspDidChangeTsOrJsFileParams)
}

@ApiStatus.Experimental
object SvelteLspExecutableDownloader : LspServerDownloader(svelteLspServerPackageDescriptor()) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).lspServerPackageRef
  }
}

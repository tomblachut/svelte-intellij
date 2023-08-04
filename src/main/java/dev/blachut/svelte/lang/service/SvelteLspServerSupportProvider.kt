// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.jetbrains.annotations.ApiStatus

val svelteLspServerPackageDescriptor = LspServerPackageDescriptor("svelte-language-server",
                                                                  "0.15.16",
                                                                  "/bin/server.js")

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isServiceEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(SvelteLspServerDescriptor(project))
    }
  }
}

/**
 * @see SvelteLspTypeScriptService
 */
class SvelteLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, svelteLspServerPackageDescriptor, "Svelte") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForService(file)
}

@ApiStatus.Experimental
object SvelteLspExecutableDownloader : LspServerDownloader(svelteLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).packageRef
  }
}

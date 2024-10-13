// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.JSServiceSetActivationRule
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.TSPluginDownloader
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isSvelteProjectContext
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.jetbrains.annotations.ApiStatus


private object SvelteLspServerPackageDescriptor : LspServerPackageDescriptor(
  "svelte-language-server",
  "0.17.1",
  "/bin/server.js"
) {
  override val defaultVersion: String get() = Registry.stringValue("svelte.language.server.default.version")
}

@ApiStatus.Experimental
object SvelteLspExecutableDownloader : LspServerDownloader(SvelteLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).lspServerPackageRef
  }
}

private object SvelteTypeScriptPluginDescriptor : LspServerPackageDescriptor(
  "typescript-svelte-plugin",
  "0.3.42",
  ""
) {
  override val defaultVersion: String get() = Registry.stringValue("svelte.typescript.plugin.default.version")
}

@ApiStatus.Experimental
object SvelteTypeScriptPluginPackageDownloader : TSPluginDownloader(SvelteTypeScriptPluginDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).tsPluginPackageRef
  }
}

object SvelteServiceSetActivationRule : JSServiceSetActivationRule(SvelteLspExecutableDownloader, SvelteTypeScriptPluginPackageDownloader) {
  override fun isFileAcceptableForLspServer(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return isSvelteContext(file)
  }

  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isSvelteProjectContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getSvelteServiceSettings(project).serviceMode == SvelteServiceMode.ENABLED
  }
}
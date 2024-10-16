// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.JSServiceSetActivationRule
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.lang.typescript.lsp.TSPluginLoader
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
  PackageVersion.bundled<SvelteLspServerPackageDescriptor>("0.17.2", "svelte", "src/main/svelte-language-server") {
    Registry.`is`("svelte.language.server.bundled.enabled")
  },
  "/bin/server.js"
) {
  override val registryVersion: String get() = Registry.stringValue("svelte.language.server.default.version")
}

@ApiStatus.Experimental
object SvelteLspServerLoader : LspServerLoader(SvelteLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).lspServerPackageRef
  }
}

private object SvelteTSPluginPackageDescriptor : LspServerPackageDescriptor(
  "typescript-svelte-plugin",
  PackageVersion.bundled<SvelteLspServerPackageDescriptor>("0.3.42", "svelte", "src/main/typescript-svelte-plugin") {
    Registry.`is`("svelte.language.server.bundled.enabled")
  },
  "" // the relative path must remain empty because TSPlugin is expected to resolve to the whole package directory
) {
  override val registryVersion: String get() = Registry.stringValue("svelte.typescript.plugin.default.version")
}

@ApiStatus.Experimental
object SvelteTSPluginLoader : TSPluginLoader(SvelteTSPluginPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).tsPluginPackageRef
  }
}

object SvelteServiceSetActivationRule : JSServiceSetActivationRule(SvelteLspServerLoader, SvelteTSPluginLoader) {
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
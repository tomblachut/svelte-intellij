// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.LspServerActivationRule
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.lang.typescript.lsp.TSPluginActivationRule
import com.intellij.lang.typescript.lsp.TSPluginLoader
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isSvelteProjectContext
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.jetbrains.annotations.ApiStatus


private const val SVELTE_LSP_SERVER_VERSION = "0.18.1"
private val bundledSvelteLspServerVersion = SemVer.parseFromText(SVELTE_LSP_SERVER_VERSION)!!

private object SvelteLspServerPackageDescriptor : LspServerPackageDescriptor(
  "svelte-language-server",
  PackageVersion.bundled<SvelteLspServerPackageDescriptor>(SVELTE_LSP_SERVER_VERSION, "svelte", "src/main/svelte-language-server") {
    Registry.`is`("svelte.language.server.bundled.enabled")
  },
  "/bin/server.js"
) {
  override val registryVersion: String get() = Registry.stringValue("svelte.language.server.default.version")
}

internal fun isBundledSvelteLspServerSelected(project: Project): Boolean {
  if (getSvelteServiceSettings(project).lspServerPackage.systemDependentPath != defaultPackageKey) return false
  if (!Registry.`is`("svelte.language.server.bundled.enabled")) return false

  val selectedVersion = SemVer.parseFromText(Registry.stringValue("svelte.language.server.default.version"))
                        ?: bundledSvelteLspServerVersion
  return selectedVersion == bundledSvelteLspServerVersion
}

@ApiStatus.Experimental
object SvelteLspServerLoader : LspServerLoader(SvelteLspServerPackageDescriptor) {
  override fun getSelectedPackage(project: Project): NodePackage {
    return getSvelteServiceSettings(project).lspServerPackage
  }
}

private object SvelteTSPluginPackageDescriptor : LspServerPackageDescriptor(
  "typescript-svelte-plugin",
  PackageVersion.bundled<SvelteLspServerPackageDescriptor>("0.3.52", "svelte", "src/main/typescript-svelte-plugin") {
    Registry.`is`("svelte.language.server.bundled.enabled")
  },
  "" // the relative path must remain empty because TSPlugin is expected to resolve to the whole package directory
) {
  override val registryVersion: String get() = Registry.stringValue("svelte.typescript.plugin.default.version")
}

@ApiStatus.Experimental
object SvelteTSPluginLoader : TSPluginLoader(SvelteTSPluginPackageDescriptor) {
  override fun getSelectedPackage(project: Project): NodePackage {
    return getSvelteServiceSettings(project).tsPluginPackage
  }
}

object SvelteTSPluginActivationRule : TSPluginActivationRule(SvelteTSPluginLoader, SvelteActivationHelper)

object SvelteLspServerActivationRule : LspServerActivationRule(SvelteLspServerLoader, SvelteActivationHelper) {
  override fun isFileAcceptable(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return isSvelteContext(file)
  }
}

private object SvelteActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isSvelteProjectContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getSvelteServiceSettings(project).serviceMode == SvelteServiceMode.ENABLED
  }
}
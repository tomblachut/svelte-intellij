package dev.blachut.svelte.lang.service

import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

private val plugin = DownloadableTypeScriptServicePlugin("Svelte", SvelteTypeScriptPluginPackageDownloader)

class SveltePluginTypeScriptService(project: Project) : PluggableTypeScriptService(project, plugin) {
  override fun hasDependenciesReady(context: VirtualFile): Boolean {
    return isTypeScriptPluginEnabledAndAvailable(project, context)
  }
}
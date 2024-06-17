package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.TSPluginDownloader
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.jetbrains.annotations.ApiStatus


private object SvelteTypeScriptPluginDescriptor : LspServerPackageDescriptor("typescript-svelte-plugin",
                                                                             "0.3.38",
                                                                             "") {
  override val defaultVersion: String get() = Registry.stringValue("svelte.typescript.plugin.default.version")
}

@ApiStatus.Experimental
object SvelteTypeScriptPluginPackageDownloader : TSPluginDownloader(SvelteTypeScriptPluginDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).tsPluginPackageRef
  }
}

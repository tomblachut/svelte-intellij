package dev.blachut.svelte.lang.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.jetbrains.annotations.ApiStatus


val svelteTypeScriptPluginDescriptor = LspServerPackageDescriptor("typescript-svelte-plugin",
                                                                  "0.3.34",
                                                                  "")

@ApiStatus.Experimental
object SvelteTypeScriptPluginPackageDownloader : LspServerDownloader(svelteTypeScriptPluginDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getSvelteServiceSettings(project).tsPluginPackageRef
  }
}

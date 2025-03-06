package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import dev.blachut.svelte.lang.service.settings.SvelteServiceConfigurable
import icons.SvelteIcons

private val plugin = DownloadableTypeScriptServicePlugin("Svelte", SvelteServiceSetActivationRule)

class SveltePluginTypeScriptService(project: Project) : PluggableTypeScriptService(project, plugin) {
  override fun getProcessName(): String =
  // can't use instance fields here when legacy JSAsyncLanguageServiceBase.myToolWindowManager is not null
    // and JSAsyncLanguageServiceBase.createDefaultReporter calls getProcessName() during <init>
    "Svelte + TypeScript" // todo replace with: "${plugin.shortLabel} + TypeScript"

  override fun getInitialOpenCommands(): List<JSLanguageServiceSimpleCommand> {
    return listOf(createConfigureCommand()) + super.getInitialOpenCommands()
  }

  private fun createConfigureCommand(): JSLanguageServiceSimpleCommand {
    // https://github.com/sveltejs/language-tools/pull/2185
    val arguments = ConfigurePluginRequestArguments(pluginName = "typescript-svelte-plugin",
                                                    configuration = mapOf("name" to "typescript-svelte-plugin",
                                                                          "enabled" to true,
                                                                          "assumeIsSvelteProject" to true))
    return ConfigurePluginRequest(arguments)
  }

  override fun isServiceFallbackResolveEnabled(): Boolean = true

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, SvelteIcons.Original, SvelteIcons.Desaturated, SvelteServiceConfigurable::class.java)

  override fun isTypeEvaluationEnabled(): Boolean = false
}
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
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
import java.util.function.Consumer

private val plugin = DownloadableTypeScriptServicePlugin("Svelte", SvelteServiceSetActivationRule)

class SveltePluginTypeScriptService(project: Project) : PluggableTypeScriptService(project, plugin) {
  override fun getProcessName(): String =
    // can't use instance fields here when legacy JSAsyncLanguageServiceBase.myToolWindowManager is not null
    // and JSAsyncLanguageServiceBase.createDefaultReporter calls getProcessName() during <init>
    "Svelte + TypeScript" // todo replace with: "${plugin.shortLabel} + TypeScript"

  override fun getInitialOpenCommands(): MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> {
    val initialCommands = super.getInitialOpenCommands()
    val result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> = linkedMapOf()
    addConfigureCommand(result)

    result.putAll(initialCommands)
    return result
  }

  private fun addConfigureCommand(result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>>) {
    // https://github.com/sveltejs/language-tools/pull/2185
    val arguments = ConfigurePluginRequestArguments(pluginName = "typescript-svelte-plugin",
                                                    configuration = mapOf("name" to "typescript-svelte-plugin",
                                                                          "enabled" to true,
                                                                          "assumeIsSvelteProject" to true))
    result[ConfigurePluginRequest(arguments)] = Consumer {}
  }

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, SvelteIcons.Original, SvelteServiceConfigurable::class.java)
}
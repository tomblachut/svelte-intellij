package dev.blachut.svelte.lang.service

import com.intellij.javascript.typeEngine.JSServicePoweredTypeEngineUsageContext
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.service.settings.SvelteServiceConfigurable
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import icons.SvelteIcons

private val plugin = DownloadableTypeScriptServicePlugin("Svelte", SvelteTSPluginActivationRule)

class SveltePluginTypeScriptService(project: Project) : PluggableTypeScriptService(project, plugin) {
  override fun getProcessName(): String =
  // can't use instance fields here when legacy JSAsyncLanguageServiceBase.myToolWindowManager is not null
    // and JSAsyncLanguageServiceBase.createDefaultReporter calls getProcessName() during <init>
    "Svelte + TypeScript" // todo replace with: "${plugin.shortLabel} + TypeScript"

  override fun createProtocol(tsServicePath: String): TypeScriptServiceStandardOutputProtocol {
    return SvelteTypeScriptServiceProtocol(
      project = project,
      settings = mySettings,
      eventConsumer = createEventConsumer(),
      serviceName = serviceName,
      tsServicePath = tsServicePath,
      servicePlugin = servicePlugin,
    )
  }

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

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is SvelteJSLanguage || file.language is SvelteTypeScriptLanguage
  }

  override fun isTypeEvaluationEnabled(): Boolean =
    getSvelteServiceSettings(project).useTypesFromServer

  override val typeEvaluationSupport: TypeScriptServiceEvaluationSupport =
    SvelteCompilerServiceEvaluationSupport(project)

  /**
   * Custom evaluation support for Svelte that enables service-powered type engine.
   * Similar to VueCompilerServiceEvaluationSupport and Angular2CompilerServiceEvaluationSupport.
   */
  private inner class SvelteCompilerServiceEvaluationSupport(
    project: Project,
  ) : TypeScriptCompilerServiceEvaluationSupport(project) {

    override val service: TypeScriptService
      get() = this@SveltePluginTypeScriptService

    override fun isEnabledInUsageContext(usageContext: JSServicePoweredTypeEngineUsageContext): Boolean = true
  }
}
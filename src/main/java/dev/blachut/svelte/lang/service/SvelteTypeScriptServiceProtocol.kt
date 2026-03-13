// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.psi.util.JSPluginPathManager.getPluginResource
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptServiceProtocol
import com.intellij.openapi.project.Project
import java.io.IOException
import java.nio.file.Path
import java.util.function.Consumer

internal class SvelteTypeScriptServiceProtocol(
  project: Project,
  settings: TypeScriptCompilerSettings,
  eventConsumer: Consumer<in JSLanguageServiceAnswer>,
  serviceName: String,
  tsServicePath: String,
  servicePlugin: DownloadableTypeScriptServicePlugin,
) : PluggableTypeScriptServiceProtocol(
  project = project,
  settings = settings,
  eventConsumer = eventConsumer,
  serviceName = serviceName,
  tsServicePath = tsServicePath,
  servicePlugin = servicePlugin,
) {
  override fun getGlobalPlugins(): List<String> {
    return super.getGlobalPlugins() + "ws-typescript-svelte-plugin"
  }

  override fun getProbeLocations(): Array<LocalFilePath> {
    val probeLocations = super.getProbeLocations()
    val pluginProbe = getSvelteServicePluginLocation().parent?.parent
                      ?: return probeLocations

    return probeLocations + LocalFilePath.create(pluginProbe.toString())
  }

  private fun getSvelteServicePluginLocation(): Path =
    try {
      getPluginResource(
        this::class.java,
        "src/main/svelte-service/node_modules/ws-typescript-svelte-plugin",
        "svelte",
      )
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
}

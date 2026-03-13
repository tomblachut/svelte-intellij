// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.util.JSLogOnceService
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.ui.TypeScriptServiceRestartService
import com.intellij.lang.typescript.lsp.NestedReadWriteProperty
import com.intellij.lang.typescript.lsp.createPackage
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import dev.blachut.svelte.lang.service.SvelteLspServerLoader
import dev.blachut.svelte.lang.service.SvelteTSPluginLoader

private val LOG = logger<SvelteServiceSettings>()

fun getSvelteServiceSettings(project: Project): SvelteServiceSettings = project.service<SvelteServiceSettings>()

@Service(Service.Level.PROJECT)
@State(name = "SvelteServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class SvelteServiceSettings(val project: Project) : SimplePersistentStateComponent<SvelteServiceState>(SvelteServiceState()) {
  var serviceMode: SvelteServiceMode
    get() = state.innerServiceMode
    set(value) {
      val changed = state.innerServiceMode != value
      state.innerServiceMode = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var lspServerPackageRef: NodePackageRef
    get() = createPackageRef(state.lspServerPackageName, SvelteLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.lspServerPackageName != refText
      state.lspServerPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var tsPluginPackageRef: NodePackageRef
    get() = createPackageRef(state.tsPluginPackageName, SvelteTSPluginLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.tsPluginPackageName != refText
      state.tsPluginPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var showA11yWarnings: Boolean by NestedReadWriteProperty(
    currentReceiver = { state },
    nestedProperty = SvelteServiceState::showA11yWarnings,
  )

  /**
   * Returns whether service-powered type engine is enabled for Svelte.
   * Checks in order: test override, manual override, registry default.
   */
  val useTypesFromServer: Boolean
    get() {
      val result = TypeScriptCompilerSettings.useTypesFromServerInTests
                   ?: useServicePoweredTypesManualOverride
                   ?: Registry.`is`("svelte.service.powered.type.engine.enabled.by.default")
      with(project.service<JSLogOnceService>()) {
        LOG.infoOnce { "'Service-powered type engine' option of SvelteServiceSettings: $result" }
      }
      return result
    }

  /**
   * Manual override for service-powered type engine setting.
   * null = use registry default, true = enabled, false = disabled
   */
  var useServicePoweredTypesManualOverride: Boolean?
    get() = when {
      state.useServicePoweredTypesEnabledManually -> true
      state.useServicePoweredTypesDisabledManually -> false
      else -> null
    }
    set(value) {
      val prevUseTypesFromServer = useTypesFromServer
      state.useServicePoweredTypesEnabledManually = value == true
      state.useServicePoweredTypesDisabledManually = value == false
      if (prevUseTypesFromServer != useTypesFromServer) {
        project.service<TypeScriptServiceRestartService>().restartServices(true)
      }
    }
}

class SvelteServiceState : BaseState() {
  var innerServiceMode: SvelteServiceMode by enum(SvelteServiceMode.ENABLED)
  var lspServerPackageName: String? by string(defaultPackageKey)
  var tsPluginPackageName: String? by string(defaultPackageKey)

  var showA11yWarnings: Boolean by property(true)

  // Service-powered type engine settings
  var useServicePoweredTypesEnabledManually: Boolean by property(false)
  var useServicePoweredTypesDisabledManually: Boolean by property(false)
}

enum class SvelteServiceMode {
  ENABLED,
  DISABLED
}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import dev.blachut.svelte.lang.service.SvelteLspServerLoader
import dev.blachut.svelte.lang.service.SvelteTSPluginLoader

fun getSvelteServiceSettings(project: Project): SvelteServiceSettings = project.service<SvelteServiceSettings>()

@Service(Service.Level.PROJECT)
@State(name = "SvelteServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class SvelteServiceSettings(val project: Project) : SimplePersistentStateComponent<SvelteServiceState>(SvelteServiceState()) {
  var serviceMode
    get() = state.innerServiceMode
    set(value) {
      val changed = state.innerServiceMode != value
      state.innerServiceMode = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var lspServerPackageRef
    get() = createPackageRef(state.lspServerPackageName, SvelteLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.lspServerPackageName != refText
      state.lspServerPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var tsPluginPackageRef
    get() = createPackageRef(state.tsPluginPackageName, SvelteTSPluginLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.tsPluginPackageName != refText
      state.tsPluginPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var showA11yWarnings by state::showA11yWarnings
}

class SvelteServiceState : BaseState() {
  var innerServiceMode by enum(SvelteServiceMode.ENABLED)
  var lspServerPackageName by string(defaultPackageKey)
  var tsPluginPackageName by string(defaultPackageKey)

  var showA11yWarnings by property(true)
}

enum class SvelteServiceMode {
  ENABLED,
  DISABLED
}
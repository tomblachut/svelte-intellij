// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

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
}

class SvelteServiceState : BaseState() {
  var innerServiceMode by enum(SvelteServiceMode.ENABLED)
}

enum class SvelteServiceMode {
  ENABLED,
  DISABLED
}
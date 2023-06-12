// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import dev.blachut.svelte.lang.SvelteBundle

class SvelteServiceConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getSvelteServiceSettings(project)

  override fun Panel.createContent() {
    group(SvelteBundle.message("svelte.service.configurable.service.group")) {
      buttonsGroup {
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.disabled"), SvelteServiceMode.DISABLED)
          contextHelp(SvelteBundle.message("svelte.service.configurable.service.disabled.help"))
        }
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.lsp"), SvelteServiceMode.ENABLED)
          contextHelp(SvelteBundle.message("svelte.service.configurable.service.lsp.help"))
        }
      }.apply {
        bind(settings::serviceMode)
      }
    }
  }

  override fun getDisplayName() = SvelteBundle.message("svelte.service.configurable.title")
}
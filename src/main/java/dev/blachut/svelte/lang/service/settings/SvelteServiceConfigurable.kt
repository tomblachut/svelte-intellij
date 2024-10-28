// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.not
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.service.SvelteLspServerLoader
import dev.blachut.svelte.lang.service.SvelteTSPluginLoader

class SvelteServiceConfigurable(val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getSvelteServiceSettings(project)

  override fun Panel.createContent() {
    group(SvelteBundle.message("svelte.service.configurable.service.group")) {
      row(SvelteBundle.message("svelte.service.configurable.service.languageServerPackage")) {
        cell(SvelteLspServerLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::lspServerPackageRef)
      }

      row(SvelteBundle.message("svelte.service.configurable.service.tsPluginPackage")) {
        cell(SvelteTSPluginLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::tsPluginPackageRef)
      }

      lateinit var radioButtonDisabled: Cell<JBRadioButton>
      buttonsGroup {
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.disabled"), SvelteServiceMode.DISABLED)
            .comment(SvelteBundle.message("svelte.service.configurable.service.disabled.help"))
            .also { radioButtonDisabled = it }
        }
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.lsp"), SvelteServiceMode.ENABLED)
            .comment(SvelteBundle.message("svelte.service.configurable.service.lsp.help"))
        }
      }.apply {
        bind(settings::serviceMode)
      }

      separator()

      row {
        checkBox(SvelteBundle.message("svelte.service.configurable.service.a11y"))
          .bindSelected(settings::showA11yWarnings)
      }.enabledIf(radioButtonDisabled.selected.not())
    }
  }

  override fun getDisplayName() = SvelteBundle.message("svelte.service.configurable.title")

  override fun getHelpTopic() = "settings.svelteservice"
}
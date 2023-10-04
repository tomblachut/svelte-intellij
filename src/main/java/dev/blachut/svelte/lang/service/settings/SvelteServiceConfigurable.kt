// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.service.SvelteLspExecutableDownloader

class SvelteServiceConfigurable(val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getSvelteServiceSettings(project)

  override fun Panel.createContent() {
    group(SvelteBundle.message("svelte.service.configurable.service.group")) {
      row(SvelteBundle.message("svelte.service.configurable.service.package")) {
        cell(SvelteLspExecutableDownloader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::packageRef)
      }


      buttonsGroup {
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.disabled"), SvelteServiceMode.DISABLED)
            .comment(SvelteBundle.message("svelte.service.configurable.service.disabled.help"))
        }
        row {
          radioButton(SvelteBundle.message("svelte.service.configurable.service.lsp"), SvelteServiceMode.ENABLED)
            .comment(SvelteBundle.message("svelte.service.configurable.service.lsp.help"))
        }
      }.apply {
        bind(settings::serviceMode)
      }
    }
  }

  override fun getDisplayName() = SvelteBundle.message("svelte.service.configurable.title")

  override fun getHelpTopic() = "settings.svelteservice"
}
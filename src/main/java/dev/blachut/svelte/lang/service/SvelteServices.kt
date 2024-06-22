// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.JSServiceSetActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isSvelteProjectContext
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings


object SvelteServiceSetActivationRule : JSServiceSetActivationRule(SvelteLspExecutableDownloader, SvelteTypeScriptPluginPackageDownloader) {
  override fun isFileAcceptableForLspServer(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return isSvelteContext(file)
  }

  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isSvelteProjectContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getSvelteServiceSettings(project).serviceMode == SvelteServiceMode.ENABLED
  }
}
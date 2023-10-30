// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isSvelteProjectContext
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings


/**
 * Checks if file is local and of the correct file type.
 */
fun isFileAcceptableForService(file: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

  return isSvelteContext(file)
}

/**
 * If enabled but not available, will launch a background task that will eventually restart the services
 */
fun isServiceEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return isFileAcceptableForService(context) &&
         TypeScriptLanguageServiceUtil.isServiceEnabled(project) &&
         !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, context) &&
         isSvelteServiceEnabledBySettings(project) &&
         isSvelteProjectContext(project, context) &&
         SvelteLspExecutableDownloader.getExecutableOrRefresh(project) != null
}

/**
 * If enabled but not available, will launch a background task that will eventually restart the services
 */
fun isTypeScriptPluginEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return TypeScriptLanguageServiceUtil.isServiceEnabled(project) &&
         !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, context) &&
         isSvelteServiceEnabledBySettings(project) &&
         isSvelteProjectContext(project, context) &&
         SvelteTypeScriptPluginPackageDownloader.getExecutableOrRefresh(project) != null
}

private fun isSvelteServiceEnabledBySettings(project: Project): Boolean {
  return getSvelteServiceSettings(project).serviceMode == SvelteServiceMode.ENABLED
}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.getLspServerExecutablePath
import com.intellij.lang.typescript.lsp.scheduleLspServerDownloading
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.jetbrains.annotations.ApiStatus

internal val svelteLanguageToolsVersion = SemVer.parseFromText("0.15.12")
internal const val npmPackage = "svelte-language-server"
private const val packageRelativePath = "/bin/server.js"
val serverPackageName = TypeScriptPackageName(npmPackage, svelteLanguageToolsVersion)

class SvelteLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    getSvelteServerDescriptor(project, file)?.let { serverStarter.ensureServerStarted(it) }
  }
}

fun getSvelteServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
  if (!isServiceEnabledAndAvailable(project, file)) return null
  val projectDir = project.guessProjectDir() ?: return null
  return SvelteLspServerDescriptor(project, projectDir)
}


class SvelteLspServerDescriptor(project: Project, vararg roots: VirtualFile)
  : JSFrameworkLspServerDescriptor(project, "Svelte", *roots) {
  override val relativeScriptPath = packageRelativePath
  override val npmPackage = serverPackageName

  override fun isSupportedFile(file: VirtualFile): Boolean {
    return isServiceEnabledAndAvailable(project, file)
  }
}

@ApiStatus.Experimental
object SvelteLspExecutableDownloader {
  fun getExecutable(): String? {
    return getLspServerExecutablePath(serverPackageName, packageRelativePath)
  }

  fun getExecutableOrRefresh(project: Project): String? {
    val executable = getExecutable()
    if (executable != null) return executable
    scheduleLspServerDownloading(project, serverPackageName)
    return null
  }
}

package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.isSvelteContext

internal class SvelteLanguageServiceProvider(project: Project) : JSLanguageServiceProvider {
  private val lspService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<SvelteServiceWrapper>() }

  override fun getAllServices(): List<JSLanguageService> = listOf(lspService.service)

  override fun getService(file: VirtualFile): JSLanguageService? = allServices.firstOrNull { it.isAcceptable(file) }

  override fun isHighlightingCandidate(file: VirtualFile): Boolean {
    return TypeScriptLanguageServiceProvider.isJavaScriptOrTypeScriptFileType(file.fileType)
           || isSvelteContext(file)
  }
}

@Service(Service.Level.PROJECT)
private class SvelteServiceWrapper(project: Project) : Disposable {
  val service = SvelteLspTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}
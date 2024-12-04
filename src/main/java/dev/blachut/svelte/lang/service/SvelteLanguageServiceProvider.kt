package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.isSvelteContext

internal class SvelteLanguageServiceProvider(project: Project) : JSLanguageServiceProvider {
  private val lspService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<SvelteLspTypeScriptServiceWrapper>() }
  private val tsService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<SveltePluginTypeScriptServiceWrapper>() }

  override val allServices: List<JSLanguageService>
    get() = listOf(lspService.service, tsService.service)

  override fun getService(file: VirtualFile): JSLanguageService? = allServices.firstOrNull { it.isAcceptable(file) }

  override fun isHighlightingCandidate(file: VirtualFile): Boolean {
    return TypeScriptLanguageServiceUtil.isJavaScriptOrTypeScriptFileType(file.fileType)
           || isSvelteContext(file)
  }
}

@Service(Service.Level.PROJECT)
private class SvelteLspTypeScriptServiceWrapper(project: Project) : Disposable {
  val service = SvelteLspTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}

@Service(Service.Level.PROJECT)
private class SveltePluginTypeScriptServiceWrapper(project: Project) : Disposable {
  val service = SveltePluginTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}
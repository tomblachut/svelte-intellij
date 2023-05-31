package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.CreateImportExportInfo
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportCandidate
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider.CandidatesFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import icons.SvelteIcons
import java.util.function.Predicate
import javax.swing.Icon

class SvelteComponentCandidatesProvider(placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  private val candidates: Map<String, List<VirtualFile>> by lazy {
    FilenameIndex.getAllFilesByExt(project, "svelte",
                                   createProjectImportsScope(placeInfo, getStructureModuleRoot(placeInfo)))
      .groupBy { getComponentName(it) }
  }

  override fun processCandidates(ref: String,
                                 processor: JSCandidatesProcessor) {
    val place = myPlaceInfo.place
    val candidates = candidates[ref]
    candidates?.forEach { processor.processCandidate(SvelteImportCandidate(ref, place, it)) }
  }

  override fun getNames(keyFilter: Predicate<in String>): Set<String> {
    return candidates.keys.filter(keyFilter::test).toSet()
  }

  private fun getComponentName(virtualFile: VirtualFile): String {
    return if (virtualFile.name == "index.svelte") {
      virtualFile.parent.name
    }
    else {
      virtualFile.nameWithoutExtension
    }
  }

  class Factory : CandidatesFactory {
    override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider {
      return SvelteComponentCandidatesProvider(placeInfo)
    }
  }
}

class SvelteImportCandidate(name: String, place: PsiElement, private val virtualFile: VirtualFile)
  : JSSimpleImportCandidate(name, null, place) {
  override fun createDescriptor(): JSImportDescriptor? {
    val place = place ?: return null
    val baseImportDescriptor = ES6CreateImportUtil.getImportDescriptor(name, null, virtualFile, place, true)
    if (baseImportDescriptor == null) return null

    // JavaScript plugin does not understand .svelte files, so it tries to import them like assets, i.e. bare import
    val info = CreateImportExportInfo(name, ES6ImportPsiUtil.ImportExportType.DEFAULT)
    return JSSimpleImportDescriptor(baseImportDescriptor.moduleDescriptor, info)
  }

  override fun getContainerText(): String {

    return descriptor?.moduleName ?: ""
  }

  override fun getIcon(flags: Int): Icon {
    return SvelteIcons.Desaturated
  }
}

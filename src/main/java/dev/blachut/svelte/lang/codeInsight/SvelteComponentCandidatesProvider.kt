package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.CreateImportExportInfo
import com.intellij.lang.ecmascript6.psi.impl.JSImportScope
import com.intellij.lang.ecmascript6.psi.impl.TypeScriptImportScope
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportCandidate
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider.CandidatesFactory
import com.intellij.lang.javascript.psi.JSModuleStatusOwner
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import dev.blachut.svelte.lang.SvelteHtmlFileType
import java.util.function.Consumer

class SvelteComponentCandidatesProvider(placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  private val candidates: Map<String, List<VirtualFile>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    FilenameIndex.getAllFilesByExt(project, "svelte", createImportScope(project, placeInfo))
      .groupBy { getComponentName(it) }
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    val place = myPlaceInfo.place
    val candidates = candidates[name] // not sorted by shortest module path yet at this line
    val manager = place.manager
    candidates?.forEach { virtualFile ->
      manager.findFile(virtualFile)?.let { psiFile ->
        processor.processCandidate(SvelteImportCandidate(name, psiFile, place))
      }
    }
  }

  override fun collectNames(consumer: Consumer<String>) {
    candidates.keys.forEach(consumer)
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

  private fun createImportScope(project: Project, placeInfo: JSImportPlaceInfo): GlobalSearchScope {
    val element = placeInfo.place
    val psiFile = element.containingFile
    val contextFile = psiFile.originalFile.virtualFile
    if (contextFile == null || !element.isValid) {
      return GlobalSearchScope.EMPTY_SCOPE
    }

    if (psiFile?.fileType !is SvelteHtmlFileType || psiFile !is XmlFile) {
      return ES6CreateImportUtil.createSymbolToImportSearchScope(project, psiFile)
    }

    val parent = contextFile.parent
    val baseScope = JSResolveUtil.getResolveScope(element)
    if (parent == null) return baseScope

    val scriptTagContent = JSUtils.findScriptTagContent(psiFile)
    return if (scriptTagContent != null && DialectDetector.isTypeScript(scriptTagContent)) {
      TypeScriptImportScope(project, contextFile, baseScope)
    } else {
      JSImportScope(project, contextFile, baseScope, JSModuleStatusOwner.ModuleStatus.ES6)
    }
  }
}

class SvelteImportCandidate(name: String, candidate: PsiFile, place: PsiElement) : JSSimpleImportCandidate(name, candidate, place) {
  override fun createDescriptor(): JSImportDescriptor? {
    val place = place ?: return null
    val elementFile = elementFile ?: return null
    val baseImportDescriptor = ES6CreateImportUtil.getImportDescriptor(name, null, elementFile, place, true)
    if (baseImportDescriptor == null) return null

    // JavaScript plugin does not understand .svelte files, so it tries to import them like assets, i.e., bare import
    val info = CreateImportExportInfo(name, ES6ImportPsiUtil.ImportExportType.DEFAULT)
    return JSSimpleImportDescriptor(baseImportDescriptor.moduleDescriptor, info)
  }
}

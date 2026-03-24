package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag
import kotlin.experimental.or

class SvelteReferencesSearch : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
  override fun processQuery(
    queryParameters: ReferencesSearch.SearchParameters,
    consumer: Processor<in PsiReference>
  ) {
    val element = queryParameters.elementToSearch
    val effectiveSearchScope = queryParameters.effectiveSearchScope

    // Some JS features limit their scope to <script> tag content, following block expands that to whole file
    if (effectiveSearchScope is LocalSearchScope) {
      val identifier = (element as? JSElement)?.name ?: return
      effectiveSearchScope.scope.forEach {
        if (it is JSTagEmbeddedContent) {
          queryParameters.optimizer.searchWord(
            identifier,
            LocalSearchScope(it.containingFile),
            (UsageSearchContext.IN_CODE or UsageSearchContext.IN_FOREIGN_LANGUAGES).toShort(),
            true,
            element
          )
        }
      }
    }

    // Search for namespaced component usages like <UI.Button> in Svelte templates.
    // Tag names like "UI.Button" are single XML tokens, so the platform's word index
    // won't find "Button" inside them. We must scan directly.
    val componentName = getComponentName(element) ?: return

    val project = element.project
    val searchScope = when (effectiveSearchScope) {
      is GlobalSearchScope -> effectiveSearchScope
      is LocalSearchScope -> GlobalSearchScope.filesScope(project, effectiveSearchScope.virtualFiles.toList())
      else -> return
    }

    val psiManager = PsiManager.getInstance(project)
    for (vFile in FileTypeIndex.getFiles(SvelteHtmlFileType, searchScope)) {
      val psiFile = psiManager.findFile(vFile) ?: continue
      PsiTreeUtil.processElements(psiFile, XmlTag::class.java) { tag ->
        val tagName = tag.name
        if (isSvelteNamespacedComponentTag(tagName) && tagName.substringAfterLast('.') == componentName) {
          val tagReference = tag.references.filterIsInstance<SvelteTagNameReference>().firstOrNull()
          if (tagReference != null) {
            consumer.process(tagReference)
          }
        }
        true
      }
    }
  }

  private fun getComponentName(element: PsiElement): String? {
    return when (element) {
      is PsiFile -> if (element.name.endsWith(".svelte")) element.virtualFile?.nameWithoutExtension else null
      is JSElement -> element.name
      else -> null
    }
  }
}

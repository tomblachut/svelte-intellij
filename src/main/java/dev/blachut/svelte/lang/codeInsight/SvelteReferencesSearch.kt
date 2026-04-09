package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
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
    // The word index already splits "UI.Button" into separate words at the dot boundary
    // (SvelteFilterLexer.scanWordsInToken + IdTableBuilding.isWordCodePoint treats '.' as non-word).
    // LowLevelSearchUtil.processTreeUp walks from the leaf XML_NAME token up to the parent
    // SvelteHtmlTag where SvelteTagNameReference lives, so isReferenceTo() filters correctly.
    val componentName = getComponentName(element) ?: return

    val searchScope = when (effectiveSearchScope) {
      is GlobalSearchScope -> GlobalSearchScope.getScopeRestrictedByFileTypes(effectiveSearchScope, SvelteHtmlFileType)
      is LocalSearchScope -> effectiveSearchScope
      else -> return
    }

    queryParameters.optimizer.searchWord(
      componentName,
      searchScope,
      UsageSearchContext.IN_FOREIGN_LANGUAGES,
      true,
      element,
      NamespacedComponentResultProcessor(element)
    )
  }

  private fun getComponentName(element: PsiElement): String? {
    return when (element) {
      is PsiFile -> if (element.name.endsWith(".svelte")) element.virtualFile?.nameWithoutExtension else null
      is JSElement -> element.name?.takeIf { it.isNotEmpty() && it[0].isUpperCase() }
      else -> null
    }
  }
}

private class NamespacedComponentResultProcessor(
  private val target: PsiElement,
) : RequestResultProcessor(target) {

  override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
    if (element !is XmlTag) return true
    if (!isSvelteNamespacedComponentTag(element.name)) return true
    for (ref in element.references) {
      ProgressManager.checkCanceled()
      if (ref.isReferenceTo(target)) {
        if (!consumer.process(ref)) return false
      }
    }
    return true
  }
}

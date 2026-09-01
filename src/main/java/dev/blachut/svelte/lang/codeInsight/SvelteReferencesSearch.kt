package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.ReferenceRange
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.isSvelteComponentTag
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
            (UsageSearchContext.IN_CODE or UsageSearchContext.IN_FOREIGN_LANGUAGES),
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

    // A package entry can re-export the component (WEB-57512). The tag then resolves to a
    // synthetic alias. SvelteTagNameReference.isReferenceTo cannot map that alias back to the
    // import, so the isReferenceTo-based requests above miss the tag. This request matches the
    // tag name against the import name instead, with no resolution. Svelte resolves a component
    // tag by identifier name, and it applies no case transformation.
    //
    // Only an import gets this fallback. Any other declaration resolves normally. A name match
    // for such a target could claim a tag that belongs to a same-named import.
    //
    // ReferencesSearch wraps results in a UniqueResultsQuery, so a reference another request
    // already produced is not reported twice.
    val declarationFile = element.containingFile
    if (isComponentImport(element)
        && declarationFile?.fileType == SvelteHtmlFileType
        && coversTemplate(effectiveSearchScope, declarationFile)) {
      queryParameters.optimizer.searchWord(
        componentName,
        LocalSearchScope(declarationFile),
        UsageSearchContext.IN_FOREIGN_LANGUAGES,
        true,
        element,
        PlainComponentResultProcessor(element, componentName)
      )
    }
  }

  private fun getComponentName(element: PsiElement): String? {
    return when (element) {
      is PsiFile -> if (element.name.endsWith(".svelte")) element.virtualFile?.nameWithoutExtension else null
      is JSElement -> element.name?.takeIf { it.isNotEmpty() && it[0].isUpperCase() }
      else -> null
    }
  }

  private fun isComponentImport(element: PsiElement): Boolean {
    return element is ES6ImportedBinding || element is ES6ImportSpecifier || element is ES6ImportSpecifierAlias
  }

  /**
   * A component tag lives in the template, outside every `<script>` block. The unused-imports
   * check passes a scope of one `<script>` block, so that scope widens to the whole file. Any
   * narrower scope keeps its meaning: a result must stay inside the scope the caller passed.
   */
  private fun coversTemplate(scope: SearchScope, file: PsiFile): Boolean {
    return when (scope) {
      is GlobalSearchScope -> file.virtualFile?.let(scope::contains) == true
      is LocalSearchScope -> scope.scope.any { (it is PsiFile || it is JSTagEmbeddedContent) && it.containingFile == file }
      else -> false
    }
  }
}

/**
 * Repeats the loop of the platform `SingleTargetRequestResultProcessor`, whose match is fixed to
 * `isReferenceTo`. Subclasses supply the tag filter and the reference match. The target can lose
 * validity between request registration and execution, so the loop checks it first.
 */
private abstract class ComponentTagResultProcessor(protected val target: PsiElement) : RequestResultProcessor(target) {
  protected abstract fun acceptsTag(tagName: String): Boolean

  protected abstract fun matches(ref: PsiReference): Boolean

  final override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
    if (!target.isValid) return false
    if (element !is XmlTag || !acceptsTag(element.name)) return true
    for (ref in element.references) {
      ProgressManager.checkCanceled()
      // Both halves of `<Foo></Foo>` carry a reference. The offset picks the one reported now.
      if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && matches(ref)) {
        if (!consumer.process(ref)) return false
      }
    }
    return true
  }
}

/**
 * Reports `<Foo />` as a usage of the import named `Foo` in the same file. The match uses the tag
 * name, not resolution. Dotted tags belong to [NamespacedComponentResultProcessor].
 */
private class PlainComponentResultProcessor(
  target: PsiElement,
  private val targetName: String,
) : ComponentTagResultProcessor(target) {

  override fun acceptsTag(tagName: String): Boolean {
    return tagName == targetName && isSvelteComponentTag(tagName) && !isSvelteNamespacedComponentTag(tagName)
  }

  override fun matches(ref: PsiReference): Boolean = ref is SvelteTagNameReference && !isShadowed(ref)

  /**
   * A same-named declaration can shadow the import, as in `{#each items as Foo}<Foo />{/each}`.
   * Resolution reports a shadow reliably: it lands on a physical declaration in the import's
   * file. The import itself resolves to a synthetic alias, which is not physical.
   */
  private fun isShadowed(ref: SvelteTagNameReference): Boolean {
    val resolved = ref.multiResolve(false).mapNotNull { it.element }
    if (resolved.any { target.manager.areElementsEquivalent(it, target) }) return false
    val targetFile = target.containingFile ?: return false
    return resolved.any { it.isPhysical && it.containingFile == targetFile }
  }
}

private class NamespacedComponentResultProcessor(
  target: PsiElement,
) : ComponentTagResultProcessor(target) {

  override fun acceptsTag(tagName: String): Boolean = isSvelteNamespacedComponentTag(tagName)

  override fun matches(ref: PsiReference): Boolean = ref.isReferenceTo(target)
}

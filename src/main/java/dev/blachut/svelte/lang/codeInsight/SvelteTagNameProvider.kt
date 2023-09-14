package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSImportCompletionUtil
import com.intellij.lang.javascript.dialects.JSHandlersFactory
import com.intellij.lang.javascript.frameworks.jsx.JSXComponentCompletionContributor
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.providers.ES6ExportedCandidatesProvider
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor
import com.intellij.lang.javascript.psi.resolve.processors.JSResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import icons.SvelteIcons
import java.util.*
import java.util.function.Predicate

// Vue plugin uses 100, it's ok for now
const val highPriority = 100.0
const val mediumPriority = 50.0

// TODO Merge with svelteBareTagLookupElements
// TODO Use XmlTagInsertHandler
val svelteNamespaceTagLookupElements = svelteTagNames.map {
  LookupElementBuilder.create(sveltePrefix + it).withIcon(SvelteIcons.Gray)
}

val slotLookupElement: LookupElementBuilder = LookupElementBuilder.create("slot").withIcon(SvelteIcons.Gray)
  .withInsertHandler(XmlTagInsertHandler.INSTANCE)

/**
 * When user auto completes after writing colon in "svelte", editor will produce i.e. "svelte:svelte:self".
 */
val svelteBareTagLookupElements = svelteTagNames.map {
  val lookupElement = LookupElementBuilder.create(it).withIcon(SvelteIcons.Gray)
  PrioritizedLookupElement.withPriority(lookupElement, mediumPriority)
}

/**
 * Feeds data for tag name completion popup, including HtmlElementInTextCompletionProvider (completion without a leading <)
 *
 * Since Svelte components follow ECMAScript scope resolution rules, it is analogous to [JSXComponentCompletionContributor]
 */
class SvelteTagNameProvider : XmlTagNameProvider {
  override fun addTagNameVariants(resultElements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
    if (tag !is SvelteHtmlTag) return

    if (svelteNamespace == namespacePrefix) {
      resultElements.addAll(svelteBareTagLookupElements)
    }
    else if (namespacePrefix.isEmpty()) {
      resultElements.addAll(svelteNamespaceTagLookupElements)
      resultElements.add(slotLookupElement)

      val collectedNames = mutableSetOf<String>()
      addLocalVariants(tag, collectedNames, resultElements)
      addExportedComponents(tag, collectedNames, resultElements)
    }
  }

  // based on JSXComponentCompletionContributor.addLocalVariants
  private fun addLocalVariants(tag: XmlTag, collectedNames: MutableSet<String>, resultElements: MutableList<LookupElement>) {
    val placeInfo = JSHandlersFactory.forElement(tag).createImportPlaceInfo(tag)
    val processor = object : JSResolveProcessor {
      override fun getName(): String? {
        return null
      }

      override fun execute(element: PsiElement, state: ResolveState): Boolean {
        // omit reactive declarations
        if (element is JSDefinitionExpression) return true

        val name = ResolveProcessor.getName(element) ?: return true
        if (isSvelteComponentTag(name) && !collectedNames.contains(name) /*&& prefixMatcher.prefixMatches(name)*/) {
          val expandedElement = ES6ExportedCandidatesProvider.expandElementAndFilter(element, placeInfo).orElse(null)
          collectedNames.add(name)
          if (expandedElement != null) {
            val lookup = createLookup(name, null, element, XmlTagInsertHandler.INSTANCE)
            resultElements.add(lookup)
          }
        }
        return true
      }
    }
    JSResolveUtil.treeWalkUp(processor, tag, null, tag)
  }

  private fun addExportedComponents(tag: SvelteHtmlTag, localNames: MutableSet<String>, resultElements: MutableList<LookupElement>) {
    val info = JSHandlersFactory.forElement(tag).createImportPlaceInfo(tag)
    val providers = JSImportCandidatesProvider.getProviders(info)
    val keyFilter = Predicate { name: String ->
      isSvelteComponentTag(name) /* && prefixMatcher.prefixMatches(name) */ && !localNames.contains(name)
    }
    JSImportCompletionUtil.processExportedElements(tag, providers, keyFilter) { elements: Collection<JSImportCandidate>, name: String ->
      val importCandidate = if (elements.size == 1) elements.firstOrNull() else null
      val element = importCandidate?.getElement()
      val lookup = createLookup(name, importCandidate, element, JSImportCompletionUtil.TAG_IMPORT_INSERT_HANDLER)
      resultElements.add(lookup)
      true
    }
  }

  private fun createLookup(name: String,
                           importCandidate: JSImportCandidate?,
                           element: PsiElement?,
                           insertHandler: InsertHandler<LookupElement>): LookupElement {
    val presentation = if (element is JSNamedElement) element.presentation else null
    val lookupObject = importCandidate ?: element
    val builder =
      if (lookupObject == null) LookupElementBuilder.create(name)
      else LookupElementBuilder.create(lookupObject, name)
    return builder
      .withTypeText(presentation?.locationString, true)
      .withIcon(SvelteIcons.Desaturated)
      .withInsertHandler(insertHandler)
      .let { PrioritizedLookupElement.withPriority(it, highPriority) }
  }
}

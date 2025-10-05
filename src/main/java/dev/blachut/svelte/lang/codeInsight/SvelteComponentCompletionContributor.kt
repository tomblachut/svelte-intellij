package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSImportCompletionUtil
import com.intellij.lang.javascript.dialects.JSHandlersFactory
import com.intellij.lang.javascript.frameworks.jsx.JSXComponentCompletionContributor
import com.intellij.lang.javascript.modules.imports.JSAutoImportSupport
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.providers.ES6ExportedCandidatesProviderBase
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor
import com.intellij.lang.javascript.psi.resolve.processors.JSResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import icons.SvelteIcons
import java.util.function.Predicate

/**
 * Analogous to [JSXComponentCompletionContributor], since Svelte components follow ECMAScript scope resolution rules.
 */
class SvelteComponentCompletionContributor : CompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    val caseInsensitiveResult = result.caseInsensitive()
    LegacyCompletionContributor.processReferences(parameters, caseInsensitiveResult) { reference, _ ->
      if (reference !is TagNameReference) return@processReferences
      val tag = reference.element as? SvelteHtmlTag ?: return@processReferences

      if (reference.isStartTagFlag && tag.namespacePrefix.isEmpty() && !isSvelteNamespacedComponentTag(tag.localName)) {
        val collectedNames: MutableSet<String> = HashSet()
        addLocalVariants(caseInsensitiveResult, tag, collectedNames)
        addExportedComponents(caseInsensitiveResult, tag, collectedNames)
      }
    }
  }

  // based on JSXComponentCompletionContributor.addLocalVariants
  private fun addLocalVariants(result: CompletionResultSet, tag: XmlTag, collectedNames: MutableSet<String>) {
    val prefixMatcher = result.prefixMatcher
    val placeInfo = JSHandlersFactory.forElement(tag).createImportPlaceInfo(tag)
    val processor = object : JSResolveProcessor {
      override fun getName(): String? {
        return null
      }

      override fun execute(element: PsiElement, state: ResolveState): Boolean {
        // omit reactive declarations
        if (element is JSDefinitionExpression) return true

        val name = ResolveProcessor.getName(element) ?: return true
        if (isSvelteComponentTag(name) && !collectedNames.contains(name) && prefixMatcher.prefixMatches(name)) {
          val expandedElement = ES6ExportedCandidatesProviderBase.expandElementAndFilter(element, placeInfo)
          collectedNames.add(name)
          if (expandedElement != null) {
            val lookup = createLookup(name, null, element, XmlTagInsertHandler.INSTANCE)
            result.addElement(lookup)
          }
        }
        return true
      }
    }
    JSResolveUtil.treeWalkUp(processor, tag, null, tag)
  }

  private fun addExportedComponents(result: CompletionResultSet, tag: XmlTag, localNames: Set<String?>) {
    val autoImportSupport = JSAutoImportSupport.getInstance(tag.project)
    val prefixMatcher = result.prefixMatcher
    val info = JSHandlersFactory.forElement(tag).createImportPlaceInfo(tag)
    val providers = JSImportCandidatesProvider.getProviders(info)
    val keyFilter = Predicate { name: String ->
      isSvelteComponentTag(name) && prefixMatcher.prefixMatches(name) && !localNames.contains(name)
    }
    JSImportCompletionUtil.processExportedElements(tag, providers, keyFilter) { candidates, name ->
      val importCandidate = if (candidates.size == 1) candidates.firstOrNull() else null
      val element = importCandidate?.element
      val lookup = createLookup(name, importCandidate, element, autoImportSupport.getTagImportInsertHandler(importCandidate))
      result.addElement(lookup)
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
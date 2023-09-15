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
 * Feeds data for tag name completion popup, including HtmlElementInTextCompletionProvider (completion without a leading <).
 *
 * For components, see [SvelteComponentCompletionContributor] due to performance reasons.
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
    }
  }
}

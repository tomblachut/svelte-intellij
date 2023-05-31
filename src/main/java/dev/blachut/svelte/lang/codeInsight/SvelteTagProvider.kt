package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.completion.JSImportCompletionUtil
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.ecma6.TypeScriptCompileTimeType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor
import com.intellij.lang.javascript.psi.resolve.processors.JSResolveProcessor
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import icons.SvelteIcons
import java.util.*
import java.util.function.Predicate

// Vue plugin uses 100, it's ok for now
const val highPriority = 100.0
const val mediumPriority = 50.0

const val svelteNamespace = "svelte"
const val sveltePrefix = "$svelteNamespace:"

val svelteTagNames = arrayOf("self", "component", "window", "body", "head", "options", "fragment")

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
 * interface XmlTagNameProvider feeds data for name completion popup
 *
 * interface XmlElementDescriptorProvider enables, among others, navigation from tag to component file
 */
class SvelteTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag): XmlElementDescriptor? {
    if (tag !is SvelteHtmlTag) return null

    if (tag.namespacePrefix == svelteNamespace && svelteTagNames.contains(tag.localName) || tag.name == "slot") {
      return SvelteElementDescriptor(tag)
    }

    if (!isSvelteComponentTag(tag.name)) return null

    return SvelteComponentTagDescriptor(tag.name, tag)
  }

  override fun addTagNameVariants(resultElements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
    if (tag !is SvelteHtmlTag) return

    if (svelteNamespace == namespacePrefix) {
      resultElements.addAll(svelteBareTagLookupElements)
    }
    else if (namespacePrefix.isEmpty()) {
      resultElements.addAll(svelteNamespaceTagLookupElements)
      resultElements.add(slotLookupElement)

      val localNames = mutableSetOf<String>()
      addLocalComponents(tag, localNames, resultElements)
      addExportedComponents(tag, localNames, resultElements)
    }
  }

  private fun addLocalComponents(tag: XmlTag, localNames: MutableSet<String>, resultElements: MutableList<LookupElement>) {
    val processor = createLocalCompletionProcessor(localNames, resultElements)
    JSResolveUtil.treeWalkUp(processor, tag, null, tag)
  }

  // based on ReactComponentCompletionContributor.createCompletionProcessor, maybe unify it?
  private fun createLocalCompletionProcessor(collectedNames: MutableSet<String>,
                                             resultElements: MutableList<LookupElement>): JSResolveProcessor {
    return object : JSResolveProcessor {
      override fun getName(): String? {
        return null
      }

      override fun execute(element: PsiElement, state: ResolveState): Boolean {
        // omit reactive declarations
        if (element is JSDefinitionExpression) return true

        val name = ResolveProcessor.getName(element) ?: return true
        if (isSvelteComponentTag(name) && !collectedNames.contains(name) /*&& prefixMatcher.prefixMatches(name)*/) {
          collectedNames.add(name)
          resultElements.add(createLookupElement(name, element, null))
        }
        return true
      }
    }
  }

  private fun addExportedComponents(tag: SvelteHtmlTag, localNames: MutableSet<String>, resultElements: MutableList<LookupElement>) {
    // todo possibly base on ReactComponentCompletionContributor.addExportedComponents
    // todo and/or JSImportCompletionUtil.processExportedElements
    // todo look into case sensitivity

    // todo handle multiple files with same name, JSImportCompletionUtil.IMPORT_PRIORITY, merge lookup elements etc

    val placeInfo = JSImportPlaceInfo(tag)

    //val lowercaseName = StringUtil.trimEnd(tag.name, CompletionUtil.DUMMY_IDENTIFIER_TRIMMED).toLowerCase()
    val keyFilter = Predicate { name: String ->
      isSvelteComponentTag(name) && !localNames.contains(name)
      // && name.toLowerCase().contains(lowercaseName)
      // && prefixMatcher.prefixMatches(name)
    }

    val providers = JSImportCandidatesProvider.getProviders(placeInfo)

    JSImportCompletionUtil.processExportedElements(tag, providers, keyFilter) { candidates: Collection<JSImportCandidate>, name ->
      var seenJSCandidate = false
      var seenSvelteCandidate = false
      var bestLookup: LookupElement? = null

      candidates.forEach { candidate ->
        if (seenJSCandidate) return@forEach

        val element = candidate.element // for Svelte files will be null, used by JSLookupElementRenderer to display useful info

        if (element != null) {
          for (declaration in ES6PsiUtil.expandElements(tag, Collections.singleton(element))) {
            // todo namespaced components will require loosening of the condition
            if (!(declaration is JSClass && declaration !is TypeScriptCompileTimeType)) continue // or Svelte file
            val recordType = declaration.jsType.asRecordType()
            // older libraries may not contain all 3 properties
            if (!recordType.propertyNames.containsAll(listOf("\$set", "\$on", "\$destroy"))) continue
            val lookupElement = createLookupElement(name, declaration, createInsertHandler(tag.containingFile, candidate))
            bestLookup = lookupElement
            seenJSCandidate = true
            break
          }
        }
        else if (!seenSvelteCandidate) {
          // JSImportCandidate for SvelteHtmlFile does not contain PsiElement
          val lookupElement = createLookupElement(name, null, createInsertHandler(tag.containingFile, candidate))
          seenSvelteCandidate = true
          bestLookup = lookupElement
        }
      }

      if (bestLookup != null) {
        resultElements.add(bestLookup!!)
      }

      true
    }
  }

  private fun createLookupElement(name: @NlsSafe String,
                                  element: PsiElement?,
                                  insertHandler: InsertHandler<LookupElement>?): LookupElement {
    //val tailText = " (${candidate.containerText})"
    val presentation = if (element is JSNamedElement) element.presentation else null

    val builder = if (element != null) LookupElementBuilder.create(element, name) else LookupElementBuilder.create(name)

    return builder
      .withIcon(SvelteIcons.Desaturated)
      //.withTailText(tailText, true)
      .withTailText(presentation?.locationString, true) // todo add space
      //.withTypeText("SvelteComponent") // can't use type text because there are false positives here
      .withInsertHandler(insertHandler)
      //.let { JSLookupElementRenderer(name, JSImportCompletionUtil.IMPORT_PRIORITY, false, null).applyToBuilder(it) } // does not resolve imports unfortunately, JSImportCompletionUtil.expandElements handles that
      .let { PrioritizedLookupElement.withPriority(it, highPriority) }
  }

  private fun createInsertHandler(containingFile: PsiFile, candidate: JSImportCandidate): InsertHandler<LookupElement>? {
    if (containingFile is JSExpressionCodeFragment) return null

    // todo e.g. React uses both XmlTagInsertHandler & addReactImportInsertHandler
    return SvelteComponentInsertHandler(candidate)
  }
}

class SvelteComponentInsertHandler(private val candidate: JSImportCandidate) : InsertHandler<LookupElement> {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    JSImportCompletionUtil.insertLookupItem(context, item, candidate, null)
  }
}

package dev.blachut.svelte.lang.directives

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.css.SvelteCssSupport
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

class ScopeReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange) :
  PsiPolyVariantReferenceBase<SvelteHtmlAttribute>(attribute, rangeInElement, false) {
  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val resolver = ResolveCache.PolyVariantResolver<ScopeReference> { ref, incomplete ->
      val place = ref.element
      val referenceName = place.directive!!.specifiers[0].text
      SvelteReactiveDeclarationsUtil.processLocalDeclarations(place, referenceName, incomplete)
    }

    return JSResolveUtil.resolve(element.containingFile, this, resolver, incompleteCode)
  }
}

@Suppress("UNUSED_PARAMETER")
fun getScopeCompletions(
  attribute: SvelteHtmlAttribute,
  parameters: CompletionParameters,
  result: CompletionResultSet,
) {
  val sink = CompletionResultSink(attribute, result.prefixMatcher)
  val processor = SinkResolveProcessor(sink)
  JSReferenceExpressionImpl.doProcessLocalDeclarations(
    attribute,
    null,
    processor,
    false,
    true,
    null
  )

  result.addAllElements(sink.resultsAsObjects)
}

class PropReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange) :
  PsiReferenceBase<SvelteHtmlAttribute>(attribute, rangeInElement, true) {
  override fun resolve(): PsiElement? {
    val attributeName = element.directive!!.specifiers[0].text
    val parent = element.parent ?: return null
    return if (isSvelteComponentTag(parent.name)) {
      // TODO component props
      null
    }
    else {
      // TODO check attribute providers
      HtmlNSDescriptorImpl.getCommonAttributeDescriptor(attributeName, parent)?.declaration
    }
  }
}

@Suppress("UNUSED_PARAMETER")
fun getPropCompletions(
  attribute: SvelteHtmlAttribute,
  parameters: CompletionParameters,
  result: CompletionResultSet,
) {
  val tag = attribute.parent ?: return
  if (!isSvelteComponentTag(tag.name)) {
    HtmlNSDescriptorImpl.getCommonAttributeDescriptors(tag)
      .filter { !it.name.startsWith("on") }
      .forEach {
        result.addElement(LookupElementBuilder.create(it.name))
      }
  }
}

class EventReference(val attribute: SvelteHtmlAttribute, rangeInElement: TextRange) :
  PsiReferenceBase<SvelteHtmlAttribute>(attribute, rangeInElement, true) {
  override fun resolve(): PsiElement? {
    val eventName = attribute.directive!!.specifiers[0].text
    // TODO check attribute providers
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptor("on$eventName", attribute.parent)?.declaration
  }
}

@Suppress("UNUSED_PARAMETER")
fun getEventCompletions(
  attribute: SvelteHtmlAttribute,
  parameters: CompletionParameters,
  result: CompletionResultSet,
) {
  HtmlNSDescriptorImpl.getCommonAttributeDescriptors(attribute.parent)
    .filter { it.name.startsWith("on") }
    .forEach {
      result.addElement(LookupElementBuilder.create(it.name.substring(2)))
    }
}

class ScopeAndClassReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange) :
  PsiMultiReference(
    listOfNotNull(
      ScopeReference(attribute, rangeInElement),
      SvelteCssSupport.getClassReference(attribute, rangeInElement),
    ).toTypedArray(),
    attribute,
  )

// Returns a CSS class reference when the optional CSS support module is available, otherwise `null`.
fun getClassReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange): PsiReference? =
  SvelteCssSupport.getClassReference(attribute, rangeInElement)

fun getClassCompletions(attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) {
  SvelteCssSupport.addClassCompletions(attribute, parameters, result)
}

class ShorthandLetReference(val attribute: SvelteHtmlAttribute, rangeInElement: TextRange) :
  PsiReferenceBase<SvelteHtmlAttribute>(attribute, rangeInElement, false) {
  override fun resolve(): PsiElement? {
    return attribute.shorthandLetImplicitParameter
  }
}

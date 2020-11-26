package dev.blachut.svelte.lang.directives

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtil
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

class ScopeReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiPolyVariantReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, false) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val resolver = ResolveCache.PolyVariantResolver<ScopeReference> { ref, _ ->
            val attribute = ref.element
            val specifier = attribute.localName

            val sink = ResolveResultSink(attribute, specifier, false, incompleteCode)
            val processor = SinkResolveProcessor(specifier, attribute, sink)
            JSResolveUtil.treeWalkUp(processor, attribute, attribute, attribute, attribute.containingFile)

            processor.resultsAsResolveResults
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

class PropReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? {
        val attributeName = element.directive!!.specifiers[0].text
        // TODO check attribute providers
        return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(attributeName, element.parent)?.declaration
    }
}

@Suppress("UNUSED_PARAMETER")
fun getPropCompletions(
    attribute: SvelteHtmlAttribute,
    parameters: CompletionParameters,
    result: CompletionResultSet,
) {
    if (isSvelteComponentTag(attribute.parent.name)) {
        // TODO completions for component props
    } else {
        HtmlNSDescriptorImpl.getCommonAttributeDescriptors(attribute.parent)
            .filter { !it.name.startsWith("on") }
            .forEach {
                result.addElement(LookupElementBuilder.create(it.name))
            }
    }
}

class EventReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? {
        val eventName = element.directive!!.specifiers[0].text
        // TODO check attribute providers
        return HtmlNSDescriptorImpl.getCommonAttributeDescriptor("on$eventName", element.parent)?.declaration
    }
}

@Suppress("UNUSED_PARAMETER")
fun getEventCompletions(
    attribute: SvelteHtmlAttribute,
    parameters: CompletionParameters,
    result: CompletionResultSet,
) {
    HtmlNSDescriptorImpl.getCommonAttributeDescriptors(attribute.parent).filter { it.name.startsWith("on") }.forEach {
        result.addElement(LookupElementBuilder.create(it.name.substring(2)))
    }
}

class ScopeAndClassReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiMultiReference(arrayOf(ScopeReference(element, rangeInElement),
        getClassReference(element, rangeInElement)), element)

fun getClassReference(element: SvelteHtmlAttribute, rangeInElement: TextRange): PsiReference {
//    val range = element.directive!!.specifiers[0].rangeInName
    val descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(element)!!
    return descriptorProvider.getStyleReference(element, rangeInElement.startOffset, rangeInElement.endOffset, true)
}

fun getClassCompletions(attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) {
    val directive = attribute.directive!!
    var reference =
        attribute.findReferenceAt(directive.specifiers[0].rangeInName.startOffset) as? HtmlCssClassOrIdReference

    if (reference == null) {
        val rangeInElement = attribute.directive!!.specifiers[0].rangeInName
        val descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(attribute)!!
        reference = descriptorProvider.getStyleReference(attribute,
            rangeInElement.startOffset,
            rangeInElement.endOffset,
            true) as? HtmlCssClassOrIdReference
    }

    if (reference != null) {
        val prefixMatcher = result.prefixMatcher
        reference.addCompletions(parameters, prefixMatcher, result::addElement)
    }
}

class ShorthandLetReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, false) {
    override fun resolve(): PsiElement? {
        return element.shorthandLetImplicitParameter
    }
}

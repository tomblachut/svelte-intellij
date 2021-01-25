package dev.blachut.svelte.lang.directives

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

open class DirectiveType(
    val prefix: String,
    val target: SvelteDirectiveUtil.DirectiveTarget,
    val targetValidator: (xmlTag: XmlTag) -> Boolean = { true },
    val modifiers: Set<String> = emptySet(),
    val nestedSpecifiers: Number? = null,
    val shorthandReferenceFactory: ((element: SvelteHtmlAttribute, rangeInElement: TextRange) -> PsiReference)?,
    val shorthandCompletionFactory: ((attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) -> Unit)?,
    val longhandReferenceFactory: ((element: SvelteHtmlAttribute, rangeInElement: TextRange) -> PsiReference)?,
    val longhandCompletionFactory: ((attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) -> Unit)?,
    val valueElementType: IElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION,
    val uniquenessSelector: Unit = Unit, // TODO
) {
    val delimitedPrefix: String get() = prefix + SvelteDirectiveUtil.DIRECTIVE_SEPARATOR

    override fun toString(): String {
        return prefix
    }
}

class TransitionInOutDirectiveType(prefix: String) : DirectiveType(
    prefix = prefix,
    target = SvelteDirectiveUtil.DirectiveTarget.ELEMENT,
    modifiers = setOf("local"),
    shorthandReferenceFactory = ::ScopeReference,
    shorthandCompletionFactory = ::getScopeCompletions,
    longhandReferenceFactory = ::ScopeReference,
    longhandCompletionFactory = ::getScopeCompletions,
)

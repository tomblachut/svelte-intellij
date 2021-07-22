package dev.blachut.svelte.lang.directives

import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil.DirectiveTarget
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes


@Suppress("MemberVisibilityCanBePrivate")
object SvelteDirectiveTypes {
    val BIND = DirectiveType(
        prefix = "bind",
        target = DirectiveTarget.BOTH,
        shorthandReferenceFactory = ::ScopeReference,
        shorthandCompletionFactory = ::getPropCompletions,
        longhandReferenceFactory = ::PropReference,
        longhandCompletionFactory = ::getPropCompletions,
        valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION // TODO only variable references
    )
    val ON = DirectiveType(
        prefix = "on",
        target = DirectiveTarget.BOTH,
        modifiers = setOf(
            "preventDefault",
            "stopPropagation",
            "capture",
            "once",
            "self",
            "passive",
            "nonpassive",
            "trusted",
        ),
        shorthandReferenceFactory = ::EventReference,
        shorthandCompletionFactory = ::getEventCompletions,
        longhandReferenceFactory = ::EventReference,
        longhandCompletionFactory = ::getEventCompletions,
    )
    val CLASS = DirectiveType(
        prefix = "class",
        target = DirectiveTarget.ELEMENT,
        shorthandReferenceFactory = ::ScopeAndClassReference,
        shorthandCompletionFactory = ::getScopeCompletions,
        longhandReferenceFactory = ::getClassReference,
        longhandCompletionFactory = ::getClassCompletions,
    )
    val USE = DirectiveType(
        prefix = "use",
        target = DirectiveTarget.ELEMENT,
        nestedSpecifiers = 1,
        shorthandReferenceFactory = ::ScopeReference,
        shorthandCompletionFactory = ::getScopeCompletions,
        longhandReferenceFactory = ::ScopeReference,
        longhandCompletionFactory = ::getScopeCompletions,
    )
    val TRANSITION = TransitionInOutDirectiveType(prefix = "transition")
    val IN = TransitionInOutDirectiveType(prefix = "in")
    val OUT = TransitionInOutDirectiveType(prefix = "out")
    val ANIMATE = DirectiveType(
        prefix = "animate",
        target = DirectiveTarget.ELEMENT,
        targetValidator = { true }, // TODO only directly in keyed each
        shorthandReferenceFactory = ::ScopeReference,
        shorthandCompletionFactory = ::getScopeCompletions,
        longhandReferenceFactory = ::ScopeReference,
        longhandCompletionFactory = ::getScopeCompletions,
    )
    val LET = DirectiveType(
        prefix = "let",
        target = DirectiveTarget.BOTH,
        targetValidator = { isSvelteComponentTag(it.name) || it.getAttributeValue("slot") != null },
        valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_PARAMETER,
        shorthandReferenceFactory = ::ShorthandLetReference,
        shorthandCompletionFactory = null,
        longhandReferenceFactory = null,
        longhandCompletionFactory = null,
    )

    val ALL = setOf(BIND, ON, CLASS, USE, TRANSITION, IN, OUT, ANIMATE, LET)
}

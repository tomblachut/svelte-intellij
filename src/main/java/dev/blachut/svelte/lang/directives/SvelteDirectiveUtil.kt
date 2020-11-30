package dev.blachut.svelte.lang.directives

import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

object SvelteDirectiveUtil {
    const val DIRECTIVE_SEPARATOR = ':'
    const val MODIFIER_SEPARATOR = '|'

    val directivePrefixes = SvelteDirectiveTypes.ALL.map { it.prefix }.toSet()

    fun getPrefixCompletions(tagName: String): List<String> {
        val target = if (isSvelteComponentTag(tagName)) DirectiveTarget.COMPONENT else DirectiveTarget.ELEMENT

        return SvelteDirectiveTypes.ALL
            .filter { it.target == DirectiveTarget.BOTH || it.target == target }
            .map { it.prefix + DIRECTIVE_SEPARATOR }
    }

    fun chooseValueElementType(attributeName: String): IElementType {
        // TODO generify
        return when (attributeName.startsWith(SvelteDirectiveTypes.LET.delimitedPrefix, true)) {
            true -> SvelteDirectiveTypes.LET.valueElementType
            false -> SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION
        }
    }

    data class Directive(
        val directiveType: DirectiveType,
        val specifiers: List<DirectiveSegment>,
        val modifiers: List<DirectiveSegment>,
    )

    data class DirectiveSegment(val text: String, val rangeInName: TextRange)

    enum class DirectiveTarget {
        BOTH, ELEMENT, COMPONENT
    }
}

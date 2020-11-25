package dev.blachut.svelte.lang.directives

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.parsing.html.SvelteAttributeNameLexer

object SvelteDirectiveSupport {
    const val DIRECTIVE_SEPARATOR = ':'
    const val MODIFIER_SEPARATOR = '|'

    val directivePrefixes = SvelteDirectiveTypes.ALL.map { it.prefix }.toSet()

    fun getPrefixCompletions(tagName: String): List<String> {
        val target = if (isSvelteComponentTag(tagName)) DirectiveTarget.COMPONENT else DirectiveTarget.ELEMENT

        return SvelteDirectiveTypes.ALL
            .filter { it.target == DirectiveTarget.BOTH || it.target == target }
            .map { it.prefix + DIRECTIVE_SEPARATOR }
    }

    fun parseDirective(attributeName: String): Directive? {
        val lexer = SvelteAttributeNameLexer()
        lexer.start(attributeName)

        if (lexer.tokenType != JSTokenTypes.IDENTIFIER) return null

        val text = lexer.tokenText.trimEnd(DIRECTIVE_SEPARATOR)

        val type = SvelteDirectiveTypes.ALL.find { it.prefix == text } ?: return null
        lexer.advance()

        val specifiers = mutableListOf<DirectiveSegment>()
        val modifiers = mutableListOf<DirectiveSegment>()

        while (lexer.tokenType != null && lexer.tokenType != JSTokenTypes.OR) {
            if (lexer.tokenType == JSTokenTypes.IDENTIFIER) {
                specifiers.add(DirectiveSegment(lexer.tokenText, TextRange(lexer.tokenStart, lexer.tokenEnd)))
            }

            lexer.advance()
        }

        if (specifiers.size == 0) {
            val offset = lexer.tokenStart
            specifiers.add(DirectiveSegment("", TextRange(offset, offset)))
        }

        while (lexer.tokenType != null) {
            if (lexer.tokenType == XmlTokenType.XML_NAME) {
                modifiers.add(DirectiveSegment(lexer.tokenText, TextRange(lexer.tokenStart, lexer.tokenEnd)))
            }

            lexer.advance()
        }

        return Directive(type, specifiers, modifiers)
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

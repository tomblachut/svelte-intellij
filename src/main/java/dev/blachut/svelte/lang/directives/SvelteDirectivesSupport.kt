package dev.blachut.svelte.lang.directives

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.parsing.html.SvelteAttributeNameLexer
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

object SvelteDirectivesSupport {
    const val DIRECTIVE_SEPARATOR = ':'
    const val MODIFIER_SEPARATOR = '|'

    val directivePrefixes by lazy {
        directiveTypes.map { it.prefix }.toSet()
    }

    private val directiveTypes: MutableSet<DirectiveType> = mutableSetOf()

    init {
        addDirectives(
            DirectiveType(
                prefix = "bind",
                target = DirectiveTarget.BOTH,
                shorthandReferenceFactory = ::ScopeReference,
                shorthandCompletionFactory = ::getScopeCompletions,
                longhandReferenceFactory = ::PropReference,
                longhandCompletionFactory = ::getPropCompletions,
                valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION // TODO only variable references
            ),
            DirectiveType(
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
                ),
                shorthandReferenceFactory = ::EventReference,
                shorthandCompletionFactory = ::getEventCompletions,
                longhandReferenceFactory = ::EventReference,
                longhandCompletionFactory = ::getEventCompletions,
            ),
            DirectiveType(
                prefix = "class",
                target = DirectiveTarget.ELEMENT,
                shorthandReferenceFactory = ::ScopeAndClassReference,
                shorthandCompletionFactory = ::getScopeCompletions,
                longhandReferenceFactory = ::getClassReference,
                longhandCompletionFactory = ::getClassCompletions,
            ),
            DirectiveType(
                prefix = "use",
                target = DirectiveTarget.ELEMENT,
                nestedSpecifiers = 1,
                shorthandReferenceFactory = ::ScopeReference,
                shorthandCompletionFactory = ::getScopeCompletions,
                longhandReferenceFactory = ::ScopeReference,
                longhandCompletionFactory = ::getScopeCompletions,
            ),
            createTransitionInOutDirectiveTypeDefinition(name = "transition"),
            createTransitionInOutDirectiveTypeDefinition(name = "in"),
            createTransitionInOutDirectiveTypeDefinition(name = "out"),
            DirectiveType(
                prefix = "animate",
                target = DirectiveTarget.ELEMENT,
                targetValidator = { true }, // TODO only directly in keyed each
                shorthandReferenceFactory = ::ScopeReference,
                shorthandCompletionFactory = ::getScopeCompletions,
                longhandReferenceFactory = ::ScopeReference,
                longhandCompletionFactory = ::getScopeCompletions,
            ),
            DirectiveType(
                prefix = "let",
                target = DirectiveTarget.BOTH,
                targetValidator = { isSvelteComponentTag(it.name) || it.getAttributeValue("slot") != null },
                valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_PARAMETER,
                shorthandReferenceFactory = ::ShorthandLetReference,
                shorthandCompletionFactory = null,
                longhandReferenceFactory = null,
                longhandCompletionFactory = null,
            ),
        )
    }

    private fun createTransitionInOutDirectiveTypeDefinition(name: String) = DirectiveType(
        prefix = name,
        target = DirectiveTarget.ELEMENT,
        modifiers = setOf("local"),
        shorthandReferenceFactory = ::ScopeReference,
        shorthandCompletionFactory = ::getScopeCompletions,
        longhandReferenceFactory = ::ScopeReference,
        longhandCompletionFactory = ::getScopeCompletions,
    )

    private fun addDirectives(vararg def: DirectiveType) {
        directiveTypes.addAll(def)
    }

    fun getPrefixes(tagName: String): List<String> {
        val target = if (isSvelteComponentTag(tagName)) DirectiveTarget.COMPONENT else DirectiveTarget.ELEMENT
        return directiveTypes
            .filter { it.target == DirectiveTarget.BOTH || it.target == target }
            .map { it.prefix + DIRECTIVE_SEPARATOR }
    }

    fun parseDirective(attributeName: String): Directive? {
        val lexer = SvelteAttributeNameLexer()
        lexer.start(attributeName)

        val text = lexer.tokenText

        val def = directiveTypes.find { it.prefix + DIRECTIVE_SEPARATOR == text } ?: return null
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

        return Directive(def, specifiers, modifiers)
    }

    data class DirectiveType(
        val prefix: String,
        val target: DirectiveTarget,
        val targetValidator: (xmlTag: XmlTag) -> Boolean = { true },
        val modifiers: Set<String> = emptySet(),
        val nestedSpecifiers: Number? = null,
        val shorthandReferenceFactory: ((element: SvelteHtmlAttribute, rangeInElement: TextRange) -> PsiReference)?,
        val shorthandCompletionFactory: ((attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) -> Unit)?,
        val longhandReferenceFactory: ((element: SvelteHtmlAttribute, rangeInElement: TextRange) -> PsiReference)?,
        val longhandCompletionFactory: ((attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) -> Unit)?,
        val valueElementType: IElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION,
        val uniquenessSelector: Unit = Unit, // TODO
    )

    enum class DirectiveTarget {
        BOTH, ELEMENT, COMPONENT
    }

    data class Directive(
        val directiveType: DirectiveType,
        val specifiers: List<DirectiveSegment>,
        val modifiers: List<DirectiveSegment>,
    )

    data class DirectiveSegment(val text: String, val rangeInName: TextRange)
}

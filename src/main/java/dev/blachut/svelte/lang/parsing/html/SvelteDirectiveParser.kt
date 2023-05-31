package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.directives.SvelteDirectiveTypes
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil.Directive

object SvelteDirectiveParser {
  fun parse(attributeName: String): Directive? {
    val lexer = SvelteDirectiveLexer()
    lexer.start(attributeName)

    if (lexer.tokenType != JSTokenTypes.IDENTIFIER) return null

    val text = lexer.tokenText.trimEnd(SvelteDirectiveUtil.DIRECTIVE_SEPARATOR)

    val type = SvelteDirectiveTypes.ALL.find { it.prefix == text } ?: return null
    lexer.advance()

    val specifiers = mutableListOf<SvelteDirectiveUtil.DirectiveSegment>()
    val modifiers = mutableListOf<SvelteDirectiveUtil.DirectiveSegment>()

    while (lexer.tokenType != null && lexer.tokenType != JSTokenTypes.OR) {
      if (lexer.tokenType == JSTokenTypes.IDENTIFIER) {
        specifiers.add(SvelteDirectiveUtil.DirectiveSegment(lexer.tokenText, TextRange(lexer.tokenStart, lexer.tokenEnd)))
      }

      lexer.advance()
    }

    if (specifiers.size == 0) {
      val offset = lexer.tokenStart
      specifiers.add(SvelteDirectiveUtil.DirectiveSegment("", TextRange(offset, offset)))
    }

    while (lexer.tokenType != null) {
      if (lexer.tokenType == XmlTokenType.XML_NAME) {
        modifiers.add(SvelteDirectiveUtil.DirectiveSegment(lexer.tokenText, TextRange(lexer.tokenStart, lexer.tokenEnd))
        )
      }

      lexer.advance()
    }

    return Directive(type, specifiers, modifiers)
  }
}

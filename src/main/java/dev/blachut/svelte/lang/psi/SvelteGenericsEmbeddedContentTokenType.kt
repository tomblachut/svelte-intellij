// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterLazyParseableNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lexer.Lexer
import com.intellij.psi.impl.source.tree.LazyParseableElement
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.psi.tree.ILightLazyParseableElementType
import com.intellij.util.CharTable
import com.intellij.util.diff.FlyweightCapableTreeStructure
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.parsing.ts.SvelteTypeScriptParserDefinition

/**
 * Token type for parsing the `generics` attribute value as TypeScript type parameters.
 *
 * Example input: `T extends { text: string }, U`
 */
class SvelteGenericsEmbeddedContentTokenType private constructor() : IElementType(
  "SVELTE_GENERICS_EMBEDDED",
  SvelteTypeScriptLanguage.INSTANCE,
  false
), EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase, ILightLazyParseableElementType {

  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return LazyParseableElement(this, text)
  }

  override fun parseContents(chameleon: ASTNode): ASTNode {
    val psi = chameleon.psi
    val project = psi.project
    val lexer = createLexer()
    val builder = PsiBuilderFactory.getInstance()
      .createBuilder(project, chameleon, lexer, language, chameleon.chars)

    parseGenericsContent(builder)
    return builder.treeBuilt.firstChildNode
  }

  override fun parseContents(chameleon: LighterLazyParseableNode): FlyweightCapableTreeStructure<LighterASTNode> {
    val file = chameleon.containingFile ?: error("Missing containing file for generics attribute")
    val project = file.project
    val lexer = createLexer()
    val builder = PsiBuilderFactory.getInstance()
      .createBuilder(project, chameleon, lexer, language, chameleon.text)

    parseGenericsContent(builder)
    return builder.lightTree
  }

  private fun createLexer(): Lexer {
    return SvelteTypeScriptParserDefinition().createLexer(null)
  }

  private fun parseGenericsContent(builder: PsiBuilder) {
    // Create root marker for the lazy parseable element itself
    val rootMarker = builder.mark()

    // Create expression content wrapper for stub support
    val expressionContent = builder.mark()

    parseTypeParameterList(builder)

    expressionContent.done(SvelteJSElementTypes.GENERICS_EXPRESSION_CONTENT)
    rootMarker.done(this)
  }

  private fun parseTypeParameterList(builder: PsiBuilder) {
    val typeParameterList = builder.mark()

    // Create TypeScript parser for accessing typeParser
    val tsParser = TypeScriptParser(builder)

    var first = true
    while (!builder.eof()) {
      if (!first) {
        // Expect comma between type parameters
        JavaScriptParserBase.checkMatches(
          builder,
          JSTokenTypes.COMMA,
          "javascript.parser.message.expected.comma"
        )

        // Allow trailing comma (TypeScript permits it in type parameter lists)
        if (builder.eof()) {
          break
        }
      }

      // Handle edge case: generics="T,,,U" (empty parameters)
      if (builder.tokenType === JSTokenTypes.COMMA) {
        if (first) first = false
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.type"))
        continue
      }

      // Parse individual type parameter using TypeScript parser
      // This handles: T, T extends Base, T extends { key: string }, T = Default
      if (!tsParser.typeParser.parseTypeParameter()) {
        if (!builder.eof()) {
          builder.advanceLexer()
        }
      }

      first = false
    }

    typeParameterList.done(SvelteJSElementTypes.SCRIPT_GENERICS_TYPE_PARAMETER_LIST)
  }

  companion object {
    @JvmField
    val INSTANCE: SvelteGenericsEmbeddedContentTokenType = SvelteGenericsEmbeddedContentTokenType()
  }
}

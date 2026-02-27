package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace
import dev.blachut.svelte.lang.langModePair

object SvelteJSLazyElementTypes {
  private val attributeParameterPair = langModePair(::createAttributeParameter)
  private val attributeExpressionPair = langModePair(::createAttributeExpression)
  private val contentExpressionPair = langModePair(::createContentExpression)
  private val spreadOrShorthandPair = langModePair(::createSpreadOrShorthand)
  private val attachExpressionPair = langModePair(::createAttachExpression)

  val ATTRIBUTE_PARAMETER: SvelteJSLazyElementType = attributeParameterPair.js
  val ATTRIBUTE_EXPRESSION = attributeExpressionPair.js
  val CONTENT_EXPRESSION = contentExpressionPair.js
  val SPREAD_OR_SHORTHAND = spreadOrShorthandPair.js
  val ATTACH_EXPRESSION = attachExpressionPair.js

  val ATTRIBUTE_PARAMETER_TS = attributeParameterPair.ts
  val ATTRIBUTE_EXPRESSION_TS = attributeExpressionPair.ts
  val CONTENT_EXPRESSION_TS = contentExpressionPair.ts
  val SPREAD_OR_SHORTHAND_TS = spreadOrShorthandPair.ts
  val ATTACH_EXPRESSION_TS = attachExpressionPair.ts

  fun getAttributeParameter(langMode: SvelteLangMode): SvelteJSLazyElementType = attributeParameterPair[langMode]
  fun getAttributeExpression(langMode: SvelteLangMode): SvelteJSLazyElementType = attributeExpressionPair[langMode]
  fun getContentExpression(langMode: SvelteLangMode): SvelteJSLazyElementType = contentExpressionPair[langMode]
  fun getSpreadOrShorthand(langMode: SvelteLangMode): SvelteJSLazyElementType = spreadOrShorthandPair[langMode]
  fun getAttachExpression(langMode: SvelteLangMode): SvelteJSLazyElementType = attachExpressionPair[langMode]

  private fun createAttributeParameter(langMode: SvelteLangMode) = object : SvelteJSLazyElementType(langMode.toElementTypeName("ATTRIBUTE_PARAMETER"), langMode) {
    override val noTokensErrorMessage = "Parameter expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      parseAtModifiersError(builder)
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  private fun createAttributeExpression(langMode: SvelteLangMode) = object : SvelteJSLazyElementType(langMode.toElementTypeName("ATTRIBUTE_EXPRESSION"), langMode) {
    override val noTokensErrorMessage = "Expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      parseAtModifiersError(builder)
      parser.expressionParser.parseExpression()
    }
  }

  /** Text expressions + html, debug & render + const */
  private fun createContentExpression(langMode: SvelteLangMode) = object : SvelteJSLazyElementType(langMode.toElementTypeName("CONTENT_EXPRESSION"), langMode) {
    override val noTokensErrorMessage = "Expression expected"
    override val assumeExternalBraces = false // for now trailing { and } belong to this token

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      if (parseAtModifiers(builder)) {
        parseSvelteDeclaringAssignmentExpression(builder, parser)
      }
      else {
        parser.expressionParser.parseExpression()
      }
    }

    private fun parseSvelteDeclaringAssignmentExpression(builder: PsiBuilder, parser: JavaScriptParser) {
      val expr: PsiBuilder.Marker = builder.mark()

      var openedPar = false
      if (builder.tokenType === JSTokenTypes.LPAR) {
        openedPar = true
        builder.advanceLexer()
      }

      val allowTypeDeclaration = langMode == SvelteLangMode.HAS_TS
      parser.statementParser.parseVarDeclaration(SvelteJSElementTypes.getConstTagVariable(langMode), allowTypeDeclaration, false)

      if (openedPar) {
        JavaScriptParserBase.checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
      }

      expr.done(JSElementTypes.VAR_STATEMENT)
    }
  }

  private fun createSpreadOrShorthand(langMode: SvelteLangMode) = object : SvelteJSLazyElementType(langMode.toElementTypeName("SPREAD_OR_SHORTHAND"), langMode) {
    override val noTokensErrorMessage = "Shorthand attribute or spread expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      parseAtModifiersError(builder)
      if (builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
        val marker = builder.mark()
        builder.advanceLexer()
        parseAtModifiersError(builder)
        parser.expressionParser.parseAssignmentExpression(false)
        marker.done(JSElementTypes.SPREAD_EXPRESSION)
      }
      else {
        parser.expressionParser.parseAssignmentExpression(false)
      }
    }
  }

  private fun createAttachExpression(langMode: SvelteLangMode) = object : SvelteJSLazyElementType(langMode.toElementTypeName("ATTACH_EXPRESSION"), langMode) {
    override val noTokensErrorMessage = "Attachment expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      // Handle @attach keyword
      if (builder.tokenType === JSTokenTypes.AT) {
        builder.advanceLexer()

        if (builder.isTokenAfterWhiteSpace()) {
          builder.error(SvelteBundle.message("svelte.parsing.error.whitespace.not.allowed.after"))
        }

        if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "attach") {
          builder.remapCurrentToken(SvelteTokenTypes.ATTACH_KEYWORD)
          builder.advanceLexer()
        }
        else {
          val errorMarker = builder.mark()
          builder.advanceLexer()
          errorMarker.error(SvelteBundle.message("svelte.parsing.error.expected.attach"))
        }
      }

      // Parse the expression (function reference, call expression, conditional, etc.)
      parser.expressionParser.parseAssignmentExpression(false)
    }
  }

  /**
   * Remaps the current identifier token to its Svelte keyword type.
   * @return `CONST` if the current token is the `const` keyword, `OTHER` for other recognized keywords, `NONE` if not recognized
   */
  private fun remapAtKeyword(builder: PsiBuilder): AtKeywordResult {
    if (builder.tokenType === JSTokenTypes.IDENTIFIER) {
      when (builder.tokenText) {
        "html" -> { builder.remapCurrentToken(SvelteTokenTypes.HTML_KEYWORD); builder.advanceLexer(); return AtKeywordResult.OTHER }
        "debug" -> { builder.remapCurrentToken(SvelteTokenTypes.DEBUG_KEYWORD); builder.advanceLexer(); return AtKeywordResult.OTHER }
        "render" -> { builder.remapCurrentToken(SvelteTokenTypes.RENDER_KEYWORD); builder.advanceLexer(); return AtKeywordResult.OTHER }
        "const" -> { builder.remapCurrentToken(SvelteTokenTypes.CONST_KEYWORD); builder.advanceLexer(); return AtKeywordResult.CONST }
        "attach" -> { builder.remapCurrentToken(SvelteTokenTypes.ATTACH_KEYWORD); builder.advanceLexer(); return AtKeywordResult.OTHER }
      }
    }
    else if (builder.tokenType === SvelteTokenTypes.CONST_KEYWORD) {
      builder.advanceLexer()
      return AtKeywordResult.CONST
    }
    return AtKeywordResult.NONE
  }

  private enum class AtKeywordResult { NONE, CONST, OTHER }

  private fun parseAtModifiers(builder: PsiBuilder): Boolean {
    val unexpectedTokens = setOf(JSTokenTypes.SHARP, JSTokenTypes.COLON, JSTokenTypes.DIV)

    var constMode = false

    if (builder.tokenType === JSTokenTypes.AT) {
      builder.advanceLexer()

      if (builder.isTokenAfterWhiteSpace()) {
        builder.error(SvelteBundle.message("svelte.parsing.error.whitespace.not.allowed.after"))
      }

      when (remapAtKeyword(builder)) {
        AtKeywordResult.CONST -> constMode = true
        AtKeywordResult.OTHER -> {}
        AtKeywordResult.NONE -> {
          val errorMarker = builder.mark()
          builder.advanceLexer()
          errorMarker.error(SvelteBundle.message("svelte.parsing.error.expected.html.debug.render.const"))
        }
      }
    }
    else if (unexpectedTokens.contains(builder.tokenType)) {
      builder.advanceLexer()

      if (builder.isTokenAfterWhiteSpace()) {
        builder.error(SvelteBundle.message("svelte.parsing.error.whitespace.not.allowed.here"))
      }
      val errorMarker = builder.mark()
      builder.advanceLexer()
      errorMarker.error(SvelteBundle.message("svelte.parsing.error.invalid.block.name"))
    }

    return constMode
  }

  private fun parseAtModifiersError(builder: PsiBuilder) {
    if (builder.tokenType === JSTokenTypes.AT) {
      val errorMarker = builder.mark()
      builder.advanceLexer()
      remapAtKeyword(builder)
      errorMarker.error(SvelteBundle.message("svelte.parsing.error.modifiers.are.not.allowed.here"))
    }
  }
}

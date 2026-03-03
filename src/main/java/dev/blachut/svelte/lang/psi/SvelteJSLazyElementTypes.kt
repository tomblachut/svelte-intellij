package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace

// region Named expression element types — use `is` checks to match both JS and TS variants

class AttributeParameterType(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTRIBUTE_PARAMETER"), langMode) {
  override val noTokensErrorMessage = "Parameter expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    parseAtModifiersError(builder)
    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }
}

class AttributeExpressionType(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTRIBUTE_EXPRESSION"), langMode) {
  override val noTokensErrorMessage = "Expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    parseAtModifiersError(builder)
    parser.expressionParser.parseExpression()
  }
}

/** Text expressions + html, debug & render + const */
class ContentExpressionType(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("CONTENT_EXPRESSION"), langMode) {
  override val noTokensErrorMessage = "Expression expected"
  override val assumeExternalBraces = false

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

class SpreadOrShorthandType(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("SPREAD_OR_SHORTHAND"), langMode) {
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

class AttachExpressionType(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTACH_EXPRESSION"), langMode) {
  override val noTokensErrorMessage = "Attachment expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
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

    parser.expressionParser.parseAssignmentExpression(false)
  }
}

// endregion

object SvelteJSLazyElementTypes {
  // Cached instances — use `is` checks on the type class to match both variants
  @JvmField val ATTRIBUTE_PARAMETER = AttributeParameterType(SvelteLangMode.NO_TS)
  @JvmField val ATTRIBUTE_EXPRESSION = AttributeExpressionType(SvelteLangMode.NO_TS)
  @JvmField val CONTENT_EXPRESSION = ContentExpressionType(SvelteLangMode.NO_TS)
  @JvmField val SPREAD_OR_SHORTHAND = SpreadOrShorthandType(SvelteLangMode.NO_TS)
  @JvmField val ATTACH_EXPRESSION = AttachExpressionType(SvelteLangMode.NO_TS)

  @JvmField val ATTRIBUTE_PARAMETER_TS = AttributeParameterType(SvelteLangMode.HAS_TS)
  @JvmField val ATTRIBUTE_EXPRESSION_TS = AttributeExpressionType(SvelteLangMode.HAS_TS)
  @JvmField val CONTENT_EXPRESSION_TS = ContentExpressionType(SvelteLangMode.HAS_TS)
  @JvmField val SPREAD_OR_SHORTHAND_TS = SpreadOrShorthandType(SvelteLangMode.HAS_TS)
  @JvmField val ATTACH_EXPRESSION_TS = AttachExpressionType(SvelteLangMode.HAS_TS)

  fun getAttributeParameter(langMode: SvelteLangMode): AttributeParameterType =
    if (langMode == SvelteLangMode.HAS_TS) ATTRIBUTE_PARAMETER_TS else ATTRIBUTE_PARAMETER
  fun getAttributeExpression(langMode: SvelteLangMode): AttributeExpressionType =
    if (langMode == SvelteLangMode.HAS_TS) ATTRIBUTE_EXPRESSION_TS else ATTRIBUTE_EXPRESSION
  fun getContentExpression(langMode: SvelteLangMode): ContentExpressionType =
    if (langMode == SvelteLangMode.HAS_TS) CONTENT_EXPRESSION_TS else CONTENT_EXPRESSION
  fun getSpreadOrShorthand(langMode: SvelteLangMode): SpreadOrShorthandType =
    if (langMode == SvelteLangMode.HAS_TS) SPREAD_OR_SHORTHAND_TS else SPREAD_OR_SHORTHAND
  fun getAttachExpression(langMode: SvelteLangMode): AttachExpressionType =
    if (langMode == SvelteLangMode.HAS_TS) ATTACH_EXPRESSION_TS else ATTACH_EXPRESSION

  // Predicate-based TokenSets for use with psiElement().withElementType(...)
  @JvmField val CONTENT_EXPRESSION_SET = TokenSet.forAllMatching { it is ContentExpressionType }
  @JvmField val SPREAD_OR_SHORTHAND_SET = TokenSet.forAllMatching { it is SpreadOrShorthandType }
  @JvmField val ATTACH_EXPRESSION_SET = TokenSet.forAllMatching { it is AttachExpressionType }
}

// region Shared parsing helpers

private enum class AtKeywordResult { NONE, CONST, OTHER }

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

internal fun parseAtModifiers(builder: PsiBuilder): Boolean {
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

internal fun parseAtModifiersError(builder: PsiBuilder) {
  if (builder.tokenType === JSTokenTypes.AT) {
    val errorMarker = builder.mark()
    builder.advanceLexer()
    remapAtKeyword(builder)
    errorMarker.error(SvelteBundle.message("svelte.parsing.error.modifiers.are.not.allowed.here"))
  }
}

// endregion

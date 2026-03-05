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

class AttributeParameterType private constructor(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTRIBUTE_PARAMETER"), langMode) {
  override val noTokensErrorMessage: String = "Parameter expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    parseAtModifiersError(builder)
    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }

  companion object {
    private val JS = AttributeParameterType(SvelteLangMode.NO_TS)
    private val TS = AttributeParameterType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): AttributeParameterType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class AttributeExpressionType private constructor(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTRIBUTE_EXPRESSION"), langMode) {
  override val noTokensErrorMessage: String = "Expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    parseAtModifiersError(builder)
    parser.expressionParser.parseExpression()
  }

  companion object {
    private val JS = AttributeExpressionType(SvelteLangMode.NO_TS)
    private val TS = AttributeExpressionType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): AttributeExpressionType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

/** Text expressions + html, debug & render + const */
class ContentExpressionType private constructor(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("CONTENT_EXPRESSION"), langMode) {
  override val noTokensErrorMessage: String = "Expression expected"
  override val assumeExternalBraces: Boolean = false

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

  companion object {
    private val JS = ContentExpressionType(SvelteLangMode.NO_TS)
    private val TS = ContentExpressionType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): ContentExpressionType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class SpreadOrShorthandType private constructor(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("SPREAD_OR_SHORTHAND"), langMode) {
  override val noTokensErrorMessage: String = "Shorthand attribute or spread expression expected"

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

  companion object {
    private val JS = SpreadOrShorthandType(SvelteLangMode.NO_TS)
    private val TS = SpreadOrShorthandType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): SpreadOrShorthandType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class AttachExpressionType private constructor(langMode: SvelteLangMode) : SvelteExpressionElementType(langMode.toElementTypeName("ATTACH_EXPRESSION"), langMode) {
  override val noTokensErrorMessage: String = "Attachment expression expected"

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

  companion object {
    private val JS = AttachExpressionType(SvelteLangMode.NO_TS)
    private val TS = AttachExpressionType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): AttachExpressionType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

// endregion

object SvelteJSLazyElementTypes {
  fun getAttributeParameter(langMode: SvelteLangMode): AttributeParameterType = AttributeParameterType.get(langMode)
  fun getAttributeExpression(langMode: SvelteLangMode): AttributeExpressionType = AttributeExpressionType.get(langMode)
  fun getContentExpression(langMode: SvelteLangMode): ContentExpressionType = ContentExpressionType.get(langMode)
  fun getSpreadOrShorthand(langMode: SvelteLangMode): SpreadOrShorthandType = SpreadOrShorthandType.get(langMode)
  fun getAttachExpression(langMode: SvelteLangMode): AttachExpressionType = AttachExpressionType.get(langMode)

  @JvmField val CONTENT_EXPRESSION_SET: TokenSet = TokenSet.forAllMatching { it is ContentExpressionType }
  @JvmField val SPREAD_OR_SHORTHAND_SET: TokenSet = TokenSet.forAllMatching { it is SpreadOrShorthandType }
  @JvmField val ATTACH_EXPRESSION_SET: TokenSet = TokenSet.forAllMatching { it is AttachExpressionType }

  // region Backward-compatible aliases for external consumers (returns JS variant)
  // Migration guide:
  // - For equality checks: replace `type == CONTENT_EXPRESSION` with `type is ContentExpressionType`
  // - For TokenSet membership: use predefined sets like CONTENT_EXPRESSION_SET, or START_TAGS/INNER_TAGS/END_TAGS
  // - For lang-aware code: use getContentExpression(langMode) to get the correct JS or TS variant

  @Deprecated("Use `is AttributeParameterType` check or getAttributeParameter(langMode)", ReplaceWith("getAttributeParameter(langMode)"))
  @JvmField val ATTRIBUTE_PARAMETER: AttributeParameterType = AttributeParameterType.get(SvelteLangMode.NO_TS)

  @Deprecated("Use `is AttributeExpressionType` check or getAttributeExpression(langMode)", ReplaceWith("getAttributeExpression(langMode)"))
  @JvmField val ATTRIBUTE_EXPRESSION: AttributeExpressionType = AttributeExpressionType.get(SvelteLangMode.NO_TS)

  @Deprecated("Use `is ContentExpressionType` check, CONTENT_EXPRESSION_SET, or getContentExpression(langMode)", ReplaceWith("getContentExpression(langMode)"))
  @JvmField val CONTENT_EXPRESSION: ContentExpressionType = ContentExpressionType.get(SvelteLangMode.NO_TS)

  @Deprecated("Use `is SpreadOrShorthandType` check, SPREAD_OR_SHORTHAND_SET, or getSpreadOrShorthand(langMode)", ReplaceWith("getSpreadOrShorthand(langMode)"))
  @JvmField val SPREAD_OR_SHORTHAND: SpreadOrShorthandType = SpreadOrShorthandType.get(SvelteLangMode.NO_TS)

  @Deprecated("Use `is AttachExpressionType` check, ATTACH_EXPRESSION_SET, or getAttachExpression(langMode)", ReplaceWith("getAttachExpression(langMode)"))
  @JvmField val ATTACH_EXPRESSION: AttachExpressionType = AttachExpressionType.get(SvelteLangMode.NO_TS)
  // endregion
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

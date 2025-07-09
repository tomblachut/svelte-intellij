package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace

object SvelteJSLazyElementTypes {
  val ATTRIBUTE_PARAMETER = object : SvelteJSLazyElementType("ATTRIBUTE_PARAMETER") {
    override val noTokensErrorMessage = "Parameter expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      parseAtModifiersError(builder)
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  val ATTRIBUTE_EXPRESSION = object : SvelteJSLazyElementType("ATTRIBUTE_EXPRESSION") {
    override val noTokensErrorMessage = "Expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      parseAtModifiersError(builder)
      parser.expressionParser.parseExpression()
    }
  }

  /**
   * Text expressions + html, debug & render + const
   */
  val CONTENT_EXPRESSION = object : SvelteJSLazyElementType("CONTENT_EXPRESSION") {
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

      parser.statementParser.parseVarDeclaration(SvelteJSElementTypes.CONST_TAG_VARIABLE, false, false)

      if (openedPar) {
        JavaScriptParserBase.checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
      }

      expr.done(JSElementTypes.VAR_STATEMENT)
    }
  }

  val SPREAD_OR_SHORTHAND = object : SvelteJSLazyElementType("SPREAD_OR_SHORTHAND") {
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

  private fun parseAtModifiers(builder: PsiBuilder): Boolean {
    val unexpectedTokens = setOf(JSTokenTypes.SHARP, JSTokenTypes.COLON, JSTokenTypes.DIV)

    var constMode = false

    if (builder.tokenType === JSTokenTypes.AT) {
      builder.advanceLexer()

      if (builder.isTokenAfterWhiteSpace()) {
        builder.error(SvelteBundle.message("svelte.parsing.error.whitespace.not.allowed.after"))
      }

      if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "html") {
        builder.remapCurrentToken(SvelteTokenTypes.HTML_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "debug") {
        builder.remapCurrentToken(SvelteTokenTypes.DEBUG_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "render") {
        builder.remapCurrentToken(SvelteTokenTypes.RENDER_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === SvelteTokenTypes.CONST_KEYWORD) {
        constMode = true
        builder.advanceLexer()
      }
      else {
        val errorMarker = builder.mark()
        builder.advanceLexer()
        errorMarker.error(SvelteBundle.message("svelte.parsing.error.expected.html.debug.render.const"))
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

      // copied from parseAtModifiers above
      if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "html") {
        builder.remapCurrentToken(SvelteTokenTypes.HTML_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "debug") {
        builder.remapCurrentToken(SvelteTokenTypes.DEBUG_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "render") {
        builder.remapCurrentToken(SvelteTokenTypes.RENDER_KEYWORD)
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "const") {
        builder.remapCurrentToken(SvelteTokenTypes.CONST_KEYWORD)
        builder.advanceLexer()
      }

      errorMarker.error(SvelteBundle.message("svelte.parsing.error.modifiers.are.not.allowed.here"))
    }
  }
}

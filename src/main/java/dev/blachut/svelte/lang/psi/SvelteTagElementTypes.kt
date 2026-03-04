package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSFunctionParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.html.SvelteTagParsing
import dev.blachut.svelte.lang.parsing.js.markupContextKey

// region Sealed interfaces for `is` checks on block type groups

sealed interface BlockStartType
sealed interface BlockInnerType

// endregion

// region Named block element types

class IfStartType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("IF_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.IF_KEYWORD

    parser.expressionParser.parseExpression()
  }
}

class ElseClauseType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("ELSE_CLAUSE"), langMode), BlockInnerType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.COLON
    SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
    builder.advanceLexer() // SvelteTokenTypes.ELSE_KEYWORD

    if (builder.tokenType === SvelteTokenTypes.IF_KEYWORD) {
      builder.advanceLexer()
      parser.expressionParser.parseExpression()
    }
  }
}

class EachStartType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("EACH_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"
  override val usesAsBinding: Boolean = true

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.remapCurrentToken(SvelteTokenTypes.EACH_KEYWORD) // todo might be okay to remove all those remapCurrentToken
    builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake EACH_KEYWORD

    parser.expressionParser.parseAssignmentExpression(false)

    if (builder.tokenType === SvelteTokenTypes.AS_KEYWORD) {
      builder.advanceLexer()
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }

    if (builder.tokenType === JSTokenTypes.COMMA) {
      builder.advanceLexer()
      // TODO disallow destructuring
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }

    if (builder.tokenType === JSTokenTypes.LPAR) {
      val keyExpressionMarker = builder.mark()
      builder.advanceLexer()
      parser.expressionParser.parseExpression()

      if (builder.tokenType === JSTokenTypes.RPAR) {
        builder.advanceLexer()
      }
      else {
        builder.error(SvelteBundle.message("svelte.parsing.error.expected.closing.brace"))
      }
      keyExpressionMarker.done(SvelteTagElementTypes.TAG_DEPENDENT_EXPRESSION)
    }
  }
}

class AwaitStartType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("AWAIT_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.AWAIT_KEYWORD

    parser.expressionParser.parseExpression()

    if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "then") {
      builder.remapCurrentToken(SvelteTokenTypes.THEN_KEYWORD)
      builder.advanceLexer()

      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }

    if (builder.tokenType === SvelteTokenTypes.CATCH_KEYWORD) {
      builder.advanceLexer()
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }
}

class ThenClauseType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("THEN_CLAUSE"), langMode), BlockInnerType {
  override val noTokensErrorMessage: String = "expression expected"
  override val usesAsBinding: Boolean = true

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.COLON
    SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
    builder.remapCurrentToken(SvelteTokenTypes.THEN_KEYWORD)
    builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake THEN_KEYWORD

    // TODO Check weird RBRACE placement
    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }
}

class CatchClauseType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("CATCH_CLAUSE"), langMode), BlockInnerType {
  override val noTokensErrorMessage: String = "expression expected"
  override val usesAsBinding: Boolean = true

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.COLON
    SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
    builder.advanceLexer() // SvelteTokenTypes.CATCH_KEYWORD

    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }
}

class KeyStartType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("KEY_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.KEY_KEYWORD

    parser.expressionParser.parseExpression()
  }
}

class SnippetStartType(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("SNIPPET_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.SNIPPET_KEYWORD

    try {
      builder.putUserData(markupContextKey, true)
      builder.putUserData(JSFunctionParser.methodsEmptinessKey, JSFunctionParser.MethodEmptiness.ALWAYS)
      val mark = builder.mark()
      parser.functionParser.parseFunctionNoMarker(JSFunctionParser.Context.SOURCE_ELEMENT, mark)
    }
    finally {
      builder.putUserData(JSFunctionParser.methodsEmptinessKey, null)
      builder.putUserData(markupContextKey, null)
    }
  }
}

// endregion

object SvelteTagElementTypes {
  @JvmField val IF_START: IfStartType = IfStartType(SvelteLangMode.NO_TS)
  @JvmField val ELSE_CLAUSE: ElseClauseType = ElseClauseType(SvelteLangMode.NO_TS)
  @JvmField val EACH_START: EachStartType = EachStartType(SvelteLangMode.NO_TS)
  @JvmField val AWAIT_START: AwaitStartType = AwaitStartType(SvelteLangMode.NO_TS)
  @JvmField val THEN_CLAUSE: ThenClauseType = ThenClauseType(SvelteLangMode.NO_TS)
  @JvmField val CATCH_CLAUSE: CatchClauseType = CatchClauseType(SvelteLangMode.NO_TS)
  @JvmField val KEY_START: KeyStartType = KeyStartType(SvelteLangMode.NO_TS)
  @JvmField val SNIPPET_START: SnippetStartType = SnippetStartType(SvelteLangMode.NO_TS)

  @JvmField val IF_START_TS: IfStartType = IfStartType(SvelteLangMode.HAS_TS)
  @JvmField val ELSE_CLAUSE_TS: ElseClauseType = ElseClauseType(SvelteLangMode.HAS_TS)
  @JvmField val EACH_START_TS: EachStartType = EachStartType(SvelteLangMode.HAS_TS)
  @JvmField val AWAIT_START_TS: AwaitStartType = AwaitStartType(SvelteLangMode.HAS_TS)
  @JvmField val THEN_CLAUSE_TS: ThenClauseType = ThenClauseType(SvelteLangMode.HAS_TS)
  @JvmField val CATCH_CLAUSE_TS: CatchClauseType = CatchClauseType(SvelteLangMode.HAS_TS)
  @JvmField val KEY_START_TS: KeyStartType = KeyStartType(SvelteLangMode.HAS_TS)
  @JvmField val SNIPPET_START_TS: SnippetStartType = SnippetStartType(SvelteLangMode.HAS_TS)

  fun getIfStart(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) IF_START_TS else IF_START
  fun getElseClause(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) ELSE_CLAUSE_TS else ELSE_CLAUSE
  fun getEachStart(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) EACH_START_TS else EACH_START
  fun getAwaitStart(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) AWAIT_START_TS else AWAIT_START
  fun getThenClause(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) THEN_CLAUSE_TS else THEN_CLAUSE
  fun getCatchClause(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) CATCH_CLAUSE_TS else CATCH_CLAUSE
  fun getKeyStart(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) KEY_START_TS else KEY_START
  fun getSnippetStart(langMode: SvelteLangMode): IElementType = if (langMode == SvelteLangMode.HAS_TS) SNIPPET_START_TS else SNIPPET_START

  val TAG_DEPENDENT_EXPRESSION: SvelteJSElementType = SvelteJSElementType("TAG_DEPENDENT_EXPRESSION")

  val IF_END: SvelteJSElementType = SvelteJSElementType("IF_END")
  val EACH_END: SvelteJSElementType = SvelteJSElementType("EACH_END")
  val AWAIT_END: SvelteJSElementType = SvelteJSElementType("AWAIT_END")
  val KEY_END: SvelteJSElementType = SvelteJSElementType("KEY_END")
  val SNIPPET_END: SvelteJSElementType = SvelteJSElementType("SNIPPET_END")

  val START_TAGS: TokenSet = TokenSet.forAllMatching { it is BlockStartType }
  val INNER_TAGS: TokenSet = TokenSet.forAllMatching { it is BlockInnerType }
  val END_TAGS: TokenSet = TokenSet.create(IF_END, EACH_END, AWAIT_END, KEY_END, SNIPPET_END)
  val INITIAL_TAGS: TokenSet = TokenSet.forAllMatching { it is BlockStartType || it is BlockInnerType }
  val TAIL_TAGS: TokenSet = TokenSet.forAllMatching { it is BlockInnerType || END_TAGS.contains(it) }
}

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

class IfStartType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("IF_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.IF_KEYWORD

    parser.expressionParser.parseExpression()
  }

  companion object {
    private val JS = IfStartType(SvelteLangMode.NO_TS)
    private val TS = IfStartType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): IfStartType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class ElseClauseType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("ELSE_CLAUSE"), langMode), BlockInnerType {
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

  companion object {
    private val JS = ElseClauseType(SvelteLangMode.NO_TS)
    private val TS = ElseClauseType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): ElseClauseType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class EachStartType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("EACH_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun setupBuilderContext(builder: PsiBuilder) {
    super.setupBuilderContext(builder)
    setupAsBindingContext(builder)
  }

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

  companion object {
    private val JS = EachStartType(SvelteLangMode.NO_TS)
    private val TS = EachStartType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): EachStartType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class AwaitStartType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("AWAIT_START"), langMode), BlockStartType {
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

  companion object {
    private val JS = AwaitStartType(SvelteLangMode.NO_TS)
    private val TS = AwaitStartType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): AwaitStartType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class ThenClauseType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("THEN_CLAUSE"), langMode), BlockInnerType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun setupBuilderContext(builder: PsiBuilder) {
    super.setupBuilderContext(builder)
    setupAsBindingContext(builder)
  }

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.COLON
    SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
    builder.remapCurrentToken(SvelteTokenTypes.THEN_KEYWORD)
    builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake THEN_KEYWORD

    // TODO Check weird RBRACE placement
    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }

  companion object {
    private val JS = ThenClauseType(SvelteLangMode.NO_TS)
    private val TS = ThenClauseType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): ThenClauseType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class CatchClauseType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("CATCH_CLAUSE"), langMode), BlockInnerType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun setupBuilderContext(builder: PsiBuilder) {
    super.setupBuilderContext(builder)
    setupAsBindingContext(builder)
  }

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.COLON
    SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
    builder.advanceLexer() // SvelteTokenTypes.CATCH_KEYWORD

    parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
  }

  companion object {
    private val JS = CatchClauseType(SvelteLangMode.NO_TS)
    private val TS = CatchClauseType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): CatchClauseType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class KeyStartType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("KEY_START"), langMode), BlockStartType {
  override val noTokensErrorMessage: String = "expression expected"

  override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
    builder.advanceLexer() // JSTokenTypes.SHARP
    SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
    builder.advanceLexer() // SvelteTokenTypes.KEY_KEYWORD

    parser.expressionParser.parseExpression()
  }

  companion object {
    private val JS = KeyStartType(SvelteLangMode.NO_TS)
    private val TS = KeyStartType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): KeyStartType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

class SnippetStartType internal constructor(langMode: SvelteLangMode) : SvelteBlockElementType(langMode.toElementTypeName("SNIPPET_START"), langMode), BlockStartType {
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

  companion object {
    private val JS = SnippetStartType(SvelteLangMode.NO_TS)
    private val TS = SnippetStartType(SvelteLangMode.HAS_TS)
    fun get(langMode: SvelteLangMode): SnippetStartType = if (langMode == SvelteLangMode.HAS_TS) TS else JS
  }
}

// endregion

object SvelteTagElementTypes {
  fun getIfStart(langMode: SvelteLangMode): IElementType = IfStartType.get(langMode)
  fun getElseClause(langMode: SvelteLangMode): IElementType = ElseClauseType.get(langMode)
  fun getEachStart(langMode: SvelteLangMode): IElementType = EachStartType.get(langMode)
  fun getAwaitStart(langMode: SvelteLangMode): IElementType = AwaitStartType.get(langMode)
  fun getThenClause(langMode: SvelteLangMode): IElementType = ThenClauseType.get(langMode)
  fun getCatchClause(langMode: SvelteLangMode): IElementType = CatchClauseType.get(langMode)
  fun getKeyStart(langMode: SvelteLangMode): IElementType = KeyStartType.get(langMode)
  fun getSnippetStart(langMode: SvelteLangMode): IElementType = SnippetStartType.get(langMode)

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

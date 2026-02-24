package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSFunctionParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.langModePair
import dev.blachut.svelte.lang.parsing.html.SvelteTagParsing
import dev.blachut.svelte.lang.parsing.js.markupContextKey

object SvelteTagElementTypes {
  private val ifStartPair = langModePair(::createIfStart)
  private val elseClausePair = langModePair(::createElseClause)
  private val eachStartPair = langModePair(::createEachStart)
  private val awaitStartPair = langModePair(::createAwaitStart)
  private val thenClausePair = langModePair(::createThenClause)
  private val catchClausePair = langModePair(::createCatchClause)
  private val keyStartPair = langModePair(::createKeyStart)
  private val snippetStartPair = langModePair(::createSnippetStart)

  val IF_START = ifStartPair.js
  val ELSE_CLAUSE = elseClausePair.js
  val EACH_START = eachStartPair.js
  val AWAIT_START = awaitStartPair.js
  val THEN_CLAUSE = thenClausePair.js
  val CATCH_CLAUSE = catchClausePair.js
  val KEY_START = keyStartPair.js
  val SNIPPET_START = snippetStartPair.js

  val IF_START_TS = ifStartPair.ts
  val ELSE_CLAUSE_TS = elseClausePair.ts
  val EACH_START_TS = eachStartPair.ts
  val AWAIT_START_TS = awaitStartPair.ts
  val THEN_CLAUSE_TS = thenClausePair.ts
  val CATCH_CLAUSE_TS = catchClausePair.ts
  val KEY_START_TS = keyStartPair.ts
  val SNIPPET_START_TS = snippetStartPair.ts

  fun getIfStart(langMode: SvelteLangMode): IElementType = ifStartPair[langMode]
  fun getElseClause(langMode: SvelteLangMode): IElementType = elseClausePair[langMode]
  fun getEachStart(langMode: SvelteLangMode): IElementType = eachStartPair[langMode]
  fun getAwaitStart(langMode: SvelteLangMode): IElementType = awaitStartPair[langMode]
  fun getThenClause(langMode: SvelteLangMode): IElementType = thenClausePair[langMode]
  fun getCatchClause(langMode: SvelteLangMode): IElementType = catchClausePair[langMode]
  fun getKeyStart(langMode: SvelteLangMode): IElementType = keyStartPair[langMode]
  fun getSnippetStart(langMode: SvelteLangMode): IElementType = snippetStartPair[langMode]

  private fun createIfStart(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("IF_START"), langMode) {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.advanceLexer() // SvelteTokenTypes.IF_KEYWORD

      parser.expressionParser.parseExpression()
    }
  }

  private fun createElseClause(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("ELSE_CLAUSE"), langMode) {
    override val noTokensErrorMessage = "expression expected"

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

  private fun createEachStart(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("EACH_START"), langMode) {
    override val noTokensErrorMessage = "expression expected"
    override val usesAsBinding = true // {#each} uses 'as' for binding

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
        keyExpressionMarker.done(TAG_DEPENDENT_EXPRESSION)
      }
    }
  }

  private fun createAwaitStart(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("AWAIT_START"), langMode) {
    override val noTokensErrorMessage = "expression expected"

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

  private fun createThenClause(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("THEN_CLAUSE"), langMode) {
    override val noTokensErrorMessage = "expression expected"
    override val usesAsBinding = true // {:then} uses binding syntax

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      builder.advanceLexer() // JSTokenTypes.COLON
      SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
      builder.remapCurrentToken(SvelteTokenTypes.THEN_KEYWORD)
      builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake THEN_KEYWORD

      // TODO Check weird RBRACE placement
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  private fun createCatchClause(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("CATCH_CLAUSE"), langMode) {
    override val noTokensErrorMessage = "expression expected"
    override val usesAsBinding = true // {:catch} uses binding syntax

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      builder.advanceLexer() // JSTokenTypes.COLON
      SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
      builder.advanceLexer() // SvelteTokenTypes.CATCH_KEYWORD

      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  private fun createKeyStart(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("KEY_START"), langMode) {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.advanceLexer() // SvelteTokenTypes.KEY_KEYWORD

      parser.expressionParser.parseExpression()
    }
  }

  private fun createSnippetStart(langMode: SvelteLangMode) = object : SvelteJSBlockLazyElementType(langMode.toElementTypeName("SNIPPET_START"), langMode) {
    override val noTokensErrorMessage = "expression expected"

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

  val TAG_DEPENDENT_EXPRESSION = SvelteJSElementType("TAG_DEPENDENT_EXPRESSION")

  val IF_END = SvelteJSElementType("IF_END")
  val EACH_END = SvelteJSElementType("EACH_END")
  val AWAIT_END = SvelteJSElementType("AWAIT_END")
  val KEY_END = SvelteJSElementType("KEY_END")
  val SNIPPET_END = SvelteJSElementType("SNIPPET_END")

  val START_TAGS = TokenSet.create(
    IF_START, EACH_START, AWAIT_START, KEY_START, SNIPPET_START,
    IF_START_TS, EACH_START_TS, AWAIT_START_TS, KEY_START_TS, SNIPPET_START_TS
  )
  val INNER_TAGS = TokenSet.create(
    ELSE_CLAUSE, THEN_CLAUSE, CATCH_CLAUSE,
    ELSE_CLAUSE_TS, THEN_CLAUSE_TS, CATCH_CLAUSE_TS
  )
  val END_TAGS = TokenSet.create(IF_END, EACH_END, AWAIT_END, KEY_END, SNIPPET_END)
  val INITIAL_TAGS = TokenSet.orSet(START_TAGS, INNER_TAGS)
  val TAIL_TAGS = TokenSet.orSet(INNER_TAGS, END_TAGS)
}

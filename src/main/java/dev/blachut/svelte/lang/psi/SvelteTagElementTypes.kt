package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.FunctionParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.parsing.html.SvelteTagParsing

object SvelteTagElementTypes {
  val IF_START = object : SvelteJSBlockLazyElementType("IF_START") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.advanceLexer() // SvelteTokenTypes.IF_KEYWORD

      parser.expressionParser.parseExpression()
    }
  }

  val ELSE_CLAUSE = object : SvelteJSBlockLazyElementType("ELSE_CLAUSE") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.COLON
      SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
      builder.advanceLexer() // SvelteTokenTypes.ELSE_KEYWORD

      if (builder.tokenType === SvelteTokenTypes.IF_KEYWORD) {
        builder.advanceLexer()
        parser.expressionParser.parseExpression()
      }
    }
  }

  val EACH_START = object : SvelteJSBlockLazyElementType("EACH_START") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.remapCurrentToken(SvelteTokenTypes.EACH_KEYWORD) // todo might be okay to remove all those remapCurrentToken
      builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake EACH_KEYWORD

      parser.expressionParser.parseExpression()

      if (builder.tokenType === SvelteTokenTypes.AS_KEYWORD) {
        builder.advanceLexer()
      }
      else {
        builder.error(SvelteBundle.message("svelte.parsing.error.as.expected"))
      }

      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)

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

  val AWAIT_START = object : SvelteJSBlockLazyElementType("AWAIT_START") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
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

  val THEN_CLAUSE = object : SvelteJSBlockLazyElementType("THEN_CLAUSE") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.COLON
      SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
      builder.remapCurrentToken(SvelteTokenTypes.THEN_KEYWORD)
      builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake THEN_KEYWORD

      // TODO Check weird RBRACE placement
      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  val CATCH_CLAUSE = object : SvelteJSBlockLazyElementType("CATCH_CLAUSE") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.COLON
      SvelteTagParsing.parseNotAllowedWhitespace(builder, ":")
      builder.advanceLexer() // SvelteTokenTypes.CATCH_KEYWORD

      parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
    }
  }

  val KEY_START = object : SvelteJSBlockLazyElementType("KEY_START") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.advanceLexer() // SvelteTokenTypes.KEY_KEYWORD

      parser.expressionParser.parseExpression()
    }
  }

  val SNIPPET_START = object : SvelteJSBlockLazyElementType("SNIPPET_START") {
    override val noTokensErrorMessage = "expression expected"

    override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
      builder.advanceLexer() // JSTokenTypes.SHARP
      SvelteTagParsing.parseNotAllowedWhitespace(builder, "#")
      builder.advanceLexer() // SvelteTokenTypes.SNIPPET_KEYWORD

      try {
        builder.putUserData(FunctionParser.methodsEmptinessKey, FunctionParser.MethodEmptiness.ALWAYS)
        val mark = builder.mark()
        parser.functionParser.parseFunctionNoMarker(FunctionParser.Context.SOURCE_ELEMENT, mark)
      }
      finally {
        builder.putUserData(FunctionParser.methodsEmptinessKey, null)
      }
    }
  }

  val TAG_DEPENDENT_EXPRESSION = SvelteJSElementType("TAG_DEPENDENT_EXPRESSION")

  val IF_END = SvelteJSElementType("IF_END")
  val EACH_END = SvelteJSElementType("EACH_END")
  val AWAIT_END = SvelteJSElementType("AWAIT_END")
  val KEY_END = SvelteJSElementType("KEY_END")
  val SNIPPET_END = SvelteJSElementType("SNIPPET_END")

  val START_TAGS = TokenSet.create(IF_START, EACH_START, AWAIT_START, KEY_START, SNIPPET_START)
  val INNER_TAGS = TokenSet.create(ELSE_CLAUSE, THEN_CLAUSE, CATCH_CLAUSE)
  val END_TAGS = TokenSet.create(IF_END, EACH_END, AWAIT_END, KEY_END, SNIPPET_END)
  val INITIAL_TAGS = TokenSet.orSet(START_TAGS, INNER_TAGS)
  val TAIL_TAGS = TokenSet.orSet(INNER_TAGS, END_TAGS)
}

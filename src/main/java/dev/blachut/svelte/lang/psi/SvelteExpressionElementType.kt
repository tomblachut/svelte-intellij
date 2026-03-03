package dev.blachut.svelte.lang.psi

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterLazyParseableNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.psi.tree.ILightLazyParseableElementType
import com.intellij.util.CharTable
import com.intellij.util.diff.FlyweightCapableTreeStructure
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.html.SvelteJSExpressionLexer
import dev.blachut.svelte.lang.parsing.html.SvelteTSExpressionLexer
import dev.blachut.svelte.lang.parsing.js.SvelteJSParser
import dev.blachut.svelte.lang.parsing.js.SvelteTSParser

/**
 * Base class for all Svelte expression element types (content expressions, attribute expressions, etc.).
 *
 * Uses `register=false` to avoid global IElementType registry pollution — instances are cached
 * per (kind, langMode) pair, not per occurrence. Consumers use `is` checks on the concrete
 * subclass to match both JS and TS variants.
 *
 * Modeled after [SvelteGenericsEmbeddedContentTokenType] and Vue's `VueEmbeddedContentTokenType`.
 */
abstract class SvelteExpressionElementType(
  debugName: String,
  val langMode: SvelteLangMode = SvelteLangMode.NO_TS,
) : IElementType(debugName, langMode.exprLang, false),
  EmbeddingElementType,
  ICustomParsingType,
  ILazyParseableElementTypeBase,
  ILightLazyParseableElementType {

  protected abstract val noTokensErrorMessage: String
  protected open val excessTokensErrorMessage = "Unexpected token"
  protected open val assumeExternalBraces: Boolean = true

  // ICustomParsingType — called by PsiBuilder when a collapsed token is materialized
  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return SvelteJSLazyPsiElement(this, text)
  }

  // ILazyParseableElementTypeBase — heavy AST parse path
  override fun parseContents(chameleon: ASTNode): ASTNode {
    val psi = chameleon.psi
    val project = psi.project
    val lexer = when (langMode) {
      SvelteLangMode.HAS_TS -> SvelteTSExpressionLexer(assumeExternalBraces)
      else -> SvelteJSExpressionLexer(assumeExternalBraces)
    }
    val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, langMode.exprLang, chameleon.chars)
    val startTime = System.nanoTime()

    parseContent(builder)

    val result = builder.treeBuilt.firstChildNode
    ParsingDiagnostics.registerParse(builder, language, System.nanoTime() - startTime)
    return result
  }

  // ILightLazyParseableElementType — light tree parse path
  override fun parseContents(chameleon: LighterLazyParseableNode): FlyweightCapableTreeStructure<LighterASTNode> {
    val file = chameleon.containingFile ?: error("Missing containing file")
    val project = file.project
    val lexer = when (langMode) {
      SvelteLangMode.HAS_TS -> SvelteTSExpressionLexer(assumeExternalBraces)
      else -> SvelteJSExpressionLexer(assumeExternalBraces)
    }
    val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.text)

    parseContent(builder)

    return builder.lightTree
  }

  private fun parseContent(builder: PsiBuilder) {
    setupBuilderContext(builder)

    val parser = when (langMode) {
      SvelteLangMode.HAS_TS -> SvelteTSParser(builder)
      else -> SvelteJSParser(builder)
    }

    val rootMarker = builder.mark()

    if (builder.eof()) {
      builder.error(noTokensErrorMessage)
    }
    else {
      if (!assumeExternalBraces) {
        builder.remapCurrentToken(JSTokenTypes.LBRACE)
        builder.advanceLexer()
      }
      parseTokens(builder, parser)
      if (!assumeExternalBraces) {
        remapClosingBrace(builder)
        builder.advanceLexer()
      }

      ensureEof(builder)
    }

    rootMarker.done(this)
  }

  protected open fun setupBuilderContext(builder: PsiBuilder) {}

  protected open fun remapClosingBrace(builder: PsiBuilder) {
    if (builder.tokenType == SvelteTokenTypes.END_MUSTACHE) {
      builder.remapCurrentToken(JSTokenTypes.RBRACE)
    }
  }

  protected abstract fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser)

  protected open fun ensureEof(builder: PsiBuilder) {
    if (!builder.eof()) {
      builder.error(excessTokensErrorMessage)
      // todo merge back into SvelteTagParsing.finishTag
      while (!builder.eof() && builder.tokenType !== SvelteTokenTypes.END_MUSTACHE) {
        builder.advanceLexer()
      }
      if (builder.tokenType === SvelteTokenTypes.END_MUSTACHE) {
        builder.remapCurrentToken(JSTokenTypes.RBRACE)
        builder.advanceLexer()
      }
    }
  }
}

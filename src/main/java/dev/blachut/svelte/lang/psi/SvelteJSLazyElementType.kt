package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.html.SvelteJSExpressionLexer
import dev.blachut.svelte.lang.parsing.html.SvelteTSExpressionLexer
import dev.blachut.svelte.lang.parsing.js.SvelteJSParser
import dev.blachut.svelte.lang.parsing.js.SvelteTSParser

abstract class SvelteJSLazyElementType(
  debugName: String,
  private val langMode: SvelteLangMode = SvelteLangMode.NO_TS
) : ILazyParseableElementType(debugName, langMode.exprLang) {
  protected abstract val noTokensErrorMessage: String
  protected open val excessTokensErrorMessage = "Unexpected token"

  protected open val assumeExternalBraces: Boolean = true

  override fun createNode(text: CharSequence?): ASTNode? {
    text ?: return null
    return SvelteJSLazyPsiElement(this, text)
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val project = psi.project
    val lexer = when (langMode) {
      SvelteLangMode.HAS_TS -> SvelteTSExpressionLexer(assumeExternalBraces)
      else -> SvelteJSExpressionLexer(assumeExternalBraces)
    }
    val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, langMode.exprLang, chameleon.chars)
    val startTime = System.nanoTime()

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

    val result = builder.treeBuilt.firstChildNode
    ParsingDiagnostics.registerParse(builder, language, System.nanoTime() - startTime)
    return result
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

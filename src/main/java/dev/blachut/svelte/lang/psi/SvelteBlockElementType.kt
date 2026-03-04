package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.util.CharTable
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.js.blockContextKey
import dev.blachut.svelte.lang.parsing.js.blockWithAsBindingKey

/**
 * Base class for Svelte block element types ({#if}, {#each}, {:else}, etc.).
 * Blocks include their own braces and may use 'as' for Svelte binding syntax.
 */
abstract class SvelteBlockElementType(
  debugName: String,
  langMode: SvelteLangMode = SvelteLangMode.NO_TS,
) : SvelteExpressionElementType(debugName, langMode) {

  override val assumeExternalBraces: Boolean = false

  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return SvelteInitialTag(this, text)
  }

  override fun setupBuilderContext(builder: PsiBuilder) {
    builder.putUserData(blockContextKey, true)
  }

  /**
   * Sets [blockWithAsBindingKey] so that [dev.blachut.svelte.lang.parsing.js.SvelteTSParser]
   * treats top-level 'as' as Svelte binding syntax, not TypeScript type assertion.
   * Call from [setupBuilderContext] in block types that use 'as' ({#each}, {:then}, {:catch}).
   */
  protected fun setupAsBindingContext(builder: PsiBuilder) {
    builder.putUserData(blockWithAsBindingKey, true)
  }

  override fun remapClosingBrace(builder: PsiBuilder) {
    builder.remapCurrentToken(JSTokenTypes.RBRACE)
  }

  override fun ensureEof(builder: PsiBuilder) {
    if (!builder.eof()) {
      builder.error(excessTokensErrorMessage)
      while (!builder.eof()) {
        builder.advanceLexer()
      }
    }
  }
}

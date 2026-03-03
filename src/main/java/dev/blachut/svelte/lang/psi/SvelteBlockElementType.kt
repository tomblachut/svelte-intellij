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

  /**
   * When true, top-level 'as' in TypeScript mode is treated as Svelte syntax, not type assertions.
   * Applies to {#each}, {#await}, {:then}, {:catch} blocks.
   */
  protected open val usesAsBinding: Boolean = false

  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return SvelteInitialTag(this, text)
  }

  override fun setupBuilderContext(builder: PsiBuilder) {
    builder.putUserData(blockContextKey, true)
    if (usesAsBinding) {
      builder.putUserData(blockWithAsBindingKey, true)
    }
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

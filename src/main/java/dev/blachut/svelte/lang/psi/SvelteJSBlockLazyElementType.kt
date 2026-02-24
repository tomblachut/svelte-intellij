package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.js.blockContextKey
import dev.blachut.svelte.lang.parsing.js.blockWithAsBindingKey

abstract class SvelteJSBlockLazyElementType(
  debugName: String,
  langMode: SvelteLangMode = SvelteLangMode.NO_TS
) : SvelteJSLazyElementType(debugName, langMode) {
  override val assumeExternalBraces: Boolean = false

  /**
   * Indicates whether this block uses 'as' for Svelte binding syntax.
   * When true, top-level 'as' in TypeScript mode should be treated as Svelte syntax, not type assertions.
   * This applies to {#each}, {#await}, {:then}, {:catch} blocks.
   */
  protected open val usesAsBinding: Boolean = false

  override fun createNode(text: CharSequence?): ASTNode? {
    text ?: return null
    return SvelteInitialTag(this, text)
  }

  override fun setupBuilderContext(builder: PsiBuilder) {
    builder.putUserData(blockContextKey, true)
    if (usesAsBinding) {
      builder.putUserData(blockWithAsBindingKey, true)
    }
  }

  override fun remapClosingBrace(builder: PsiBuilder) {
    builder.remapCurrentToken(com.intellij.lang.javascript.JSTokenTypes.RBRACE)
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

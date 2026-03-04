package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILeafElementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteLangMode

/**
 * Zero-length marker token emitted at the end of lexing to encode the detected language mode.
 * This allows the parser to know whether to use JS or TS for expressions.
 */
class SvelteLangModeMarkerElementType(
  val langMode: SvelteLangMode,
) : IElementType("SVELTE_LANG_MODE_$langMode", SvelteHTMLLanguage.INSTANCE),
    ILeafElementType {

  override fun createLeafNode(leafText: CharSequence): ASTNode =
    LeafPsiElement(this, leafText)
}

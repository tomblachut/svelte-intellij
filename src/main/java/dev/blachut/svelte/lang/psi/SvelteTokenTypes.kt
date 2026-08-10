package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSKeywordElementType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object SvelteTokenTypes {
  @JvmField
  val CODE_FRAGMENT: IElementType = SvelteElementType("CODE_FRAGMENT")

  @JvmField
  val START_MUSTACHE: IElementType = SvelteElementType("START_MUSTACHE")

  @JvmField
  val END_MUSTACHE: IElementType = SvelteElementType("END_MUSTACHE")

  /**
   * A JavaScript-style `//` comment inside a start tag header, e.g. `<div // comment`.
   * Svelte accepts these wherever an attribute name may start; the comment runs to the end of the line,
   * so it also swallows a `>` or `/>` placed on the same line.
   *
   * See [sveltejs/svelte#17671](https://github.com/sveltejs/svelte/pull/17671).
   */
  @JvmField
  val TAG_LINE_COMMENT: IElementType = SvelteElementType("TAG_LINE_COMMENT")

  /**
   * A JavaScript-style slash-star block comment inside a start tag header. Unlike [TAG_LINE_COMMENT]
   * it can be terminated, so attributes may follow it on the same line; an unterminated one runs to
   * the end of the file, which is how the compiler reads it as well.
   *
   * See [sveltejs/svelte#17671](https://github.com/sveltejs/svelte/pull/17671).
   */
  @JvmField
  val TAG_BLOCK_COMMENT: IElementType = SvelteElementType("TAG_BLOCK_COMMENT")

  /** Comments that may appear inside a start tag header, in place of an attribute. */
  val TAG_COMMENTS: TokenSet = TokenSet.create(TAG_LINE_COMMENT, TAG_BLOCK_COMMENT)

  @JvmField
  val IF_KEYWORD: IElementType = JSTokenTypes.IF_KEYWORD

  @JvmField
  val ELSE_KEYWORD: IElementType = JSTokenTypes.ELSE_KEYWORD

  @JvmField
  val EACH_KEYWORD: IElementType = JSTokenTypes.EACH_KEYWORD // Each is not tokenized properly in Svelte contexts

  @JvmField
  val AS_KEYWORD: IElementType = JSTokenTypes.AS_KEYWORD

  @JvmField
  val AWAIT_KEYWORD: IElementType = JSTokenTypes.AWAIT_KEYWORD

  @JvmField
  val THEN_KEYWORD: IElementType = JSKeywordElementType("then")

  @JvmField
  val CATCH_KEYWORD: IElementType = JSTokenTypes.CATCH_KEYWORD

  @JvmField
  val KEY_KEYWORD: IElementType = JSKeywordElementType("key")

  @JvmField
  val HTML_KEYWORD: IElementType = JSKeywordElementType("html")

  @JvmField
  val DEBUG_KEYWORD: IElementType = JSKeywordElementType("debug")

  @JvmField
  val CONST_KEYWORD: IElementType = JSTokenTypes.CONST_KEYWORD

  @JvmField
  val LET_KEYWORD: IElementType = JSTokenTypes.LET_KEYWORD

  @JvmField
  val SNIPPET_KEYWORD: IElementType = JSKeywordElementType("snippet")

  @JvmField
  val RENDER_KEYWORD: IElementType = JSKeywordElementType("render")

  @JvmField
  val ATTACH_KEYWORD: IElementType = JSKeywordElementType("attach")

  val KEYWORDS: TokenSet = TokenSet.create(
    IF_KEYWORD,
    ELSE_KEYWORD,
    EACH_KEYWORD,
    AS_KEYWORD,
    AWAIT_KEYWORD,
    THEN_KEYWORD,
    CATCH_KEYWORD,
    KEY_KEYWORD,
    HTML_KEYWORD,
    DEBUG_KEYWORD,
    CONST_KEYWORD,
    LET_KEYWORD,
    SNIPPET_KEYWORD,
    RENDER_KEYWORD,
    ATTACH_KEYWORD,
  )
}

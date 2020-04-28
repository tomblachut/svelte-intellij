package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSKeywordElementType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object SvelteTokenTypes {
    @JvmField
    val CODE_FRAGMENT = SvelteElementType("CODE_FRAGMENT")


    @JvmField
    val START_MUSTACHE = SvelteElementType("START_MUSTACHE")

    @JvmField
    val END_MUSTACHE = SvelteElementType("END_MUSTACHE")


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
    val THEN_KEYWORD = JSKeywordElementType("then")

    @JvmField
    val CATCH_KEYWORD: IElementType = JSTokenTypes.CATCH_KEYWORD

    val KEYWORDS = TokenSet.create(IF_KEYWORD, ELSE_KEYWORD, EACH_KEYWORD, AS_KEYWORD, AWAIT_KEYWORD, THEN_KEYWORD, CATCH_KEYWORD)
}

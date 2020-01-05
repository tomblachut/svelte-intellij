package dev.blachut.svelte.lang.psi

import com.intellij.psi.tree.IElementType

object SvelteTypes {
    @JvmField
    val CODE_FRAGMENT: IElementType = SvelteElementType("CODE_FRAGMENT")
    @JvmField
    val HTML_FRAGMENT: IElementType = SvelteElementType("HTML_FRAGMENT")

    @JvmField
    val START_MUSTACHE: IElementType = SvelteElementType("START_MUSTACHE")
    @JvmField
    val START_MUSTACHE_TEMP: IElementType = SvelteElementType("START_MUSTACHE_TEMP")
    @JvmField
    val END_MUSTACHE: IElementType = SvelteElementType("END_MUSTACHE")

    @JvmField
    val TEMP_PREFIX: IElementType = SvelteElementType("TEMP_PREFIX")
    @JvmField
    val LAZY_IF: IElementType = SvelteElementType("LAZY_IF")
    @JvmField
    val LAZY_ELSE: IElementType = SvelteElementType("LAZY_ELSE")
    @JvmField
    val LAZY_EACH: IElementType = SvelteElementType("LAZY_EACH")
    @JvmField
    val LAZY_AWAIT: IElementType = SvelteElementType("LAZY_AWAIT")
    @JvmField
    val LAZY_THEN: IElementType = SvelteElementType("LAZY_THEN")
    @JvmField
    val LAZY_CATCH: IElementType = SvelteElementType("LAZY_CATCH")

    @JvmField
    val HASH: IElementType = SvelteElementType("HASH")
    @JvmField
    val COLON: IElementType = SvelteElementType("COLON")
    @JvmField
    val SLASH: IElementType = SvelteElementType("SLASH")
    @JvmField
    val AT: IElementType = SvelteElementType("AT")
}

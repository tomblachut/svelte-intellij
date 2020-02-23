package dev.blachut.svelte.lang.psi

object SvelteTokenTypes {
    @JvmField
    val CODE_FRAGMENT = SvelteElementType("CODE_FRAGMENT")
    @JvmField
    val HTML_FRAGMENT = SvelteElementType("HTML_FRAGMENT")

    @JvmField
    val START_MUSTACHE = SvelteElementType("START_MUSTACHE")
    @JvmField
    val START_MUSTACHE_TEMP = SvelteElementType("START_MUSTACHE_TEMP")
    @JvmField
    val END_MUSTACHE = SvelteElementType("END_MUSTACHE")

    @JvmField
    val LAZY_IF = SvelteElementType("LAZY_IF")
    @JvmField
    val LAZY_ELSE = SvelteElementType("LAZY_ELSE")
    @JvmField
    val LAZY_EACH = SvelteElementType("LAZY_EACH")
    @JvmField
    val LAZY_AWAIT = SvelteElementType("LAZY_AWAIT")
    @JvmField
    val LAZY_THEN = SvelteElementType("LAZY_THEN")
    @JvmField
    val LAZY_CATCH = SvelteElementType("LAZY_CATCH")

    @JvmField
    val HASH = SvelteElementType("HASH")
    @JvmField
    val COLON = SvelteElementType("COLON")
    @JvmField
    val SLASH = SvelteElementType("SLASH")
    @JvmField
    val AT = SvelteElementType("AT")
}

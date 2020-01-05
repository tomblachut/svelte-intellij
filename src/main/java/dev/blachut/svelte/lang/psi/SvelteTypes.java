package dev.blachut.svelte.lang.psi;

import com.intellij.psi.tree.IElementType;

public interface SvelteTypes {
    IElementType CODE_FRAGMENT = new SvelteElementType("CODE_FRAGMENT");
    IElementType HTML_FRAGMENT = new SvelteElementType("HTML_FRAGMENT");

    IElementType START_MUSTACHE = new SvelteElementType("START_MUSTACHE");
    IElementType START_MUSTACHE_TEMP = new SvelteElementType("START_MUSTACHE_TEMP");
    IElementType END_MUSTACHE = new SvelteElementType("END_MUSTACHE");

    IElementType TEMP_PREFIX = new SvelteElementType("TEMP_PREFIX");
    IElementType LAZY_IF = new SvelteElementType("LAZY_IF");
    IElementType LAZY_ELSE = new SvelteElementType("LAZY_ELSE");
    IElementType LAZY_EACH = new SvelteElementType("LAZY_EACH");
    IElementType LAZY_AWAIT = new SvelteElementType("LAZY_AWAIT");
    IElementType LAZY_THEN = new SvelteElementType("LAZY_THEN");
    IElementType LAZY_CATCH = new SvelteElementType("LAZY_CATCH");

    IElementType HASH = new SvelteElementType("HASH");
    IElementType COLON = new SvelteElementType("COLON");
    IElementType SLASH = new SvelteElementType("SLASH");
    IElementType AT = new SvelteElementType("AT");
}

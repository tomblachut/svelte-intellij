package dev.blachut.svelte.lang;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.html.HTMLLanguage;

public class SvelteHTMLLanguage extends HTMLLanguage implements DependentLanguage {
    public static final SvelteHTMLLanguage INSTANCE = new SvelteHTMLLanguage();

    private SvelteHTMLLanguage() {
        super(HTMLLanguage.INSTANCE, "SvelteHTML");
    }
}

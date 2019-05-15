package dev.blachut.svelte.lang;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

// Kotlin class trips up plugin.xml inspections
public class SvelteLanguage extends Language implements TemplateLanguage {
    public static final SvelteLanguage INSTANCE = new SvelteLanguage();

    private SvelteLanguage() {
        super("Svelte");
    }
}

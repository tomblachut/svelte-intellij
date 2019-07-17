package dev.blachut.svelte.lang;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.Nullable;

// Kotlin class trips up plugin.xml inspections
public class SvelteLanguage extends Language implements TemplateLanguage {
    public static final SvelteLanguage INSTANCE = new SvelteLanguage();

    private SvelteLanguage() {
        super("Svelte");
    }

    @Nullable
    @Override
    public Language getBaseLanguage() {
        return HTMLLanguage.INSTANCE;
    }
}

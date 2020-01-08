package dev.blachut.svelte.lang;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public class SvelteHTMLLanguage extends HTMLLanguage {
    public static final SvelteHTMLLanguage INSTANCE = new SvelteHTMLLanguage();

    private SvelteHTMLLanguage() {
        super(HTMLLanguage.INSTANCE, "SvelteHTML");
    }

    @Nullable
    @Override
    public LanguageFileType getAssociatedFileType() {
        return SvelteHtmlFileType.INSTANCE;
    }
}

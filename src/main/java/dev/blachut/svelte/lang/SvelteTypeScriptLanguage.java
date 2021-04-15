package dev.blachut.svelte.lang;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;

/**
 * Always nested inside {@link SvelteHTMLLanguage}
 */
public class SvelteTypeScriptLanguage extends JSLanguageDialect implements DependentLanguage {
    public static final SvelteTypeScriptLanguage INSTANCE = new SvelteTypeScriptLanguage();

    private SvelteTypeScriptLanguage() {
        super("SvelteTS", DialectOptionHolder.TS, JavaScriptSupportLoader.TYPESCRIPT);
    }
}

package dev.blachut.svelte.lang;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import dev.blachut.svelte.lang.parsing.ts.SvelteTypeScriptParser;
import org.jetbrains.annotations.NotNull;

/**
 * Always nested inside {@link SvelteHTMLLanguage}
 */
public class SvelteTypeScriptLanguage extends JSLanguageDialect implements DependentLanguage {
    public static final SvelteTypeScriptLanguage INSTANCE = new SvelteTypeScriptLanguage();

    private SvelteTypeScriptLanguage() {
        super("SvelteTS", DialectOptionHolder.TS, JavaScriptSupportLoader.TYPESCRIPT);
    }

    @Override
    public String getFileExtension() {
        return "ts";
    }

    @Override
    public JavaScriptParser<?, ?, ?, ?> createParser(@NotNull PsiBuilder builder) {
        return new SvelteTypeScriptParser(builder);
    }
}

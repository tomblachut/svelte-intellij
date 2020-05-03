package dev.blachut.svelte.lang;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import dev.blachut.svelte.lang.parsing.js.SvelteJSParser;
import org.jetbrains.annotations.NotNull;

/**
 * Always nested inside {@link SvelteHTMLLanguage}
 */
public class SvelteJSLanguage extends JSLanguageDialect implements DependentLanguage {
    public static final SvelteJSLanguage INSTANCE = new SvelteJSLanguage();

    private SvelteJSLanguage() {
        super("SvelteJS", DialectOptionHolder.ECMA_6, JavaScriptSupportLoader.ECMA_SCRIPT_6);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }

    @Override
    public JavaScriptParser<?, ?, ?, ?> createParser(@NotNull PsiBuilder builder) {
        return new SvelteJSParser(builder);
    }
}

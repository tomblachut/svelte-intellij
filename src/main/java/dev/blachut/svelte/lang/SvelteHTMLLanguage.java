package dev.blachut.svelte.lang;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Externally it's name is just "Svelte" because it's the root language of Svelte files
 */
public final class SvelteHTMLLanguage extends HTMLLanguage {
  public static final SvelteHTMLLanguage INSTANCE = new SvelteHTMLLanguage();

  private SvelteHTMLLanguage() {
    super(HTMLLanguage.INSTANCE, "SvelteHTML");
  }

  @Override
  public @NotNull String getDisplayName() {
    return "Svelte";
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public @NotNull LanguageFileType getAssociatedFileType() {
    return SvelteHtmlFileType.INSTANCE;
  }
}

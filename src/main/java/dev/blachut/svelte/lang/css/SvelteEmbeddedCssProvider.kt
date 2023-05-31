package dev.blachut.svelte.lang.css

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteEmbeddedCssProvider : EmbeddedCssProvider() {
  override fun enableEmbeddedCssFor(language: Language): Boolean = language is SvelteHTMLLanguage
}

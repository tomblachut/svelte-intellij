package dev.blachut.svelte.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.SvelteIcons
import javax.swing.Icon

object SvelteHtmlFileType : LanguageFileType(SvelteHTMLLanguage.INSTANCE) {
  override fun getName(): String = "Svelte"
  override fun getDescription(): String = SvelteBundle.message("svelte.file.type.description")
  override fun getDefaultExtension(): String = "svelte"
  override fun getIcon(): Icon = SvelteIcons.Desaturated
}

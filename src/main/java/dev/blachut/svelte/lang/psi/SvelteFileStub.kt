package dev.blachut.svelte.lang.psi

import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlFileElementType

/**
 * File stub for Svelte files that persists the language mode.
 * This allows retrieving the lang mode without parsing the AST.
 */
class SvelteFileStub : PsiFileStubImpl<SvelteHtmlFile> {
  val langMode: SvelteLangMode

  constructor(file: SvelteHtmlFile) : super(file) {
    this.langMode = file.langMode
  }

  constructor(langMode: SvelteLangMode) : super(null) {
    this.langMode = langMode
  }

  override fun getFileElementType(): IElementType = SvelteHtmlFileElementType.FILE
}

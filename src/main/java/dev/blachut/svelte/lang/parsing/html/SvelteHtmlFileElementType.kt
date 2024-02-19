package dev.blachut.svelte.lang.parsing.html

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

// based on HtmlFileElementType
class SvelteHtmlFileElementType : IStubFileElementType<PsiFileStub<*>>("svelte file", SvelteHTMLLanguage.INSTANCE) {
  override fun getStubVersion(): Int {
    return HtmlFileElementType.getHtmlStubVersion() + SvelteJSElementTypes.STUB_VERSION
  }

  companion object {
    val FILE = SvelteHtmlFileElementType()
  }
}
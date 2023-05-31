package dev.blachut.svelte.lang.psi

import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.IXmlAttributeElementType
import com.intellij.psi.xml.IXmlTagElementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage

object SvelteHtmlElementTypes {
  val SVELTE_HTML_TAG = SvelteHtmlTagElementType("SVELTE_HTML_TAG")
  val SVELTE_HTML_ATTRIBUTE = SvelteHtmlAttributeElementType("SVELTE_HTML_ATTRIBUTE")

  class SvelteHtmlTagElementType(debugName: String) :
    IElementType(debugName, SvelteHTMLLanguage.INSTANCE),
    IXmlTagElementType

  class SvelteHtmlAttributeElementType(debugName: String) :
    IElementType(debugName, SvelteHTMLLanguage.INSTANCE),
    IXmlAttributeElementType
}

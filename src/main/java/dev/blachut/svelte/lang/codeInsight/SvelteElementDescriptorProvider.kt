package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

internal const val svelteNamespace = "svelte"
internal const val sveltePrefix = "$svelteNamespace:"
internal val svelteTagNames = arrayOf("self", "component", "window", "body", "head", "options", "fragment", "element")

/**
 * Enables, among others, navigation from tag to component file
 */
private class SvelteElementDescriptorProvider : XmlElementDescriptorProvider {
  override fun getDescriptor(tag: XmlTag): XmlElementDescriptor? {
    if (tag !is SvelteHtmlTag) return null

    if (tag.namespacePrefix == svelteNamespace && svelteTagNames.contains(tag.localName) || tag.name == "slot") {
      return SvelteElementDescriptor(tag)
    }

    if (!isSvelteComponentTag(tag.name)) return null

    return SvelteComponentTagDescriptor(tag.name, tag)
  }
}

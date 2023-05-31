package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor

class SvelteAttributeDescriptorProvider : XmlAttributeDescriptorsProvider {
  private val aAttributes = arrayOf("sveltekit:noscroll", "sveltekit:prefetch")
    .map {
      object : AnyXmlAttributeDescriptor(it) {
        override fun isEnumerated(): Boolean = true
        override fun getEnumeratedValues(): Array<String> {
          return arrayOf(it)
        }
      }
    }.toTypedArray<XmlAttributeDescriptor>()

  override fun getAttributeDescriptors(context: XmlTag): Array<XmlAttributeDescriptor> {
    if (context.name == "a") {
      return aAttributes
    }

    return XmlAttributeDescriptor.EMPTY
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    return aAttributes.find { it.name == attributeName }
  }
}

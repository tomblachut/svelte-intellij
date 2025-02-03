package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlCustomElementDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlNSDescriptor
import com.intellij.xml.impl.dtd.BaseXmlElementDescriptorImpl
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

class SvelteElementDescriptor(private val myTag: SvelteHtmlTag) : BaseXmlElementDescriptorImpl(), XmlCustomElementDescriptor {
  override fun init(element: PsiElement?) {}

  override fun getDeclaration(): PsiElement = myTag

  override fun getName(context: PsiElement?): String = myTag.name

  override fun getName(): String = myTag.name

  override fun getQualifiedName(): String = myTag.name

  override fun getDefaultName(): String = myTag.name

  override fun getNSDescriptor(): XmlNSDescriptor? = null

  override fun getContentType(): Int = CONTENT_TYPE_MIXED

  override fun collectAttributeDescriptors(context: XmlTag?): Array<XmlAttributeDescriptor> {
    return collectAttributeDescriptorsMap(context).values.toTypedArray()
  }

  override fun collectAttributeDescriptorsMap(context: XmlTag?): HashMap<String, XmlAttributeDescriptor> {
    val map = HashMap<String, XmlAttributeDescriptor>()
    myTag.attributes.forEach { map[it.name] = AnyXmlAttributeDescriptor(it.name) }

    when (myTag.name) {
      "svelte:options" -> map.putAll(optionsAttributeDescriptors)
      "svelte:component" -> map.putAll(componentAttributeDescriptors)
      "slot" -> map.putAll(slotAttributeDescriptors)
    }

    return map
  }

  override fun doCollectXmlDescriptors(context: XmlTag?): Array<XmlElementDescriptor> {
    return emptyArray()
  }

  override fun collectElementDescriptorsMap(element: XmlTag?): HashMap<String, XmlElementDescriptor> {
    return HashMap()
  }

  override fun isCustomElement(): Boolean = true

  companion object {
    private val optionsAttributeDescriptors = arrayOf(
      AnyXmlAttributeDescriptor("immutable"),
      AnyXmlAttributeDescriptor("accessors"),
      AnyXmlAttributeDescriptor("namespace"),
      AnyXmlAttributeDescriptor("tag"),
    ).associateBy { it.name }

    private val componentAttributeDescriptors = arrayOf(
      RequiredXmlAttributeDescriptor("this"),
    ).associateBy { it.name }

    private val slotAttributeDescriptors = arrayOf(
      AnyXmlAttributeDescriptor("name"),
    ).associateBy { it.name }
  }

  // TODO : BasicXmlAttributeDescriptor(), PsiPresentableMetaData
  class RequiredXmlAttributeDescriptor(attributeName: String) : AnyXmlAttributeDescriptor(attributeName) {
    override fun isRequired(): Boolean = true
  }
}

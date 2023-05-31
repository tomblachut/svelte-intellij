package dev.blachut.svelte.lang.format

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTagChild
import com.intellij.psi.xml.XmlTagValue
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlNSDescriptor
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock

class SvelteBlockFakeXmlTag(private val svelteBlock: SvelteBlock) : FakePsiElement(), XmlTag {
  override fun getParent(): PsiElement {
    return svelteBlock.parent
  }

  override fun getTextRange(): TextRange? {
    return svelteBlock.textRange
  }

  override fun getName(): String = ""

  override fun getParentTag(): XmlTag? {
    return parent as? XmlTag
  }

  override fun processElements(processor: PsiElementProcessor<PsiElement>, place: PsiElement?): Boolean {
    TODO("not implemented")
  }

  override fun getAttributes(): Array<XmlAttribute> = emptyArray()

  override fun findSubTags(qname: String?): Array<XmlTag> = emptyArray()

  override fun findSubTags(localName: String?, namespace: String?): Array<XmlTag> {
    TODO("not implemented")
  }

  override fun getNamespaceByPrefix(prefix: String?): String {
    TODO("not implemented")
  }

  override fun getSubTagText(qname: String?): String? {
    TODO("not implemented")
  }

  override fun isEmpty(): Boolean {
    TODO("not implemented")
  }

  override fun getDescriptor(): XmlElementDescriptor? {
    TODO("not implemented")
  }

  override fun getNSDescriptor(namespace: String?, strict: Boolean): XmlNSDescriptor? {
    TODO("not implemented")
  }

  override fun getAttribute(name: String?, namespace: String?): XmlAttribute? = null

  override fun getAttribute(qname: String?): XmlAttribute? = null

  override fun createChildTag(
    localName: String?,
    namespace: String?,
    bodyText: String?,
    enforceNamespacesDeep: Boolean,
  ): XmlTag {
    TODO("not implemented")
  }

  override fun collapseIfEmpty() {
  }

  override fun findFirstSubTag(qname: String?): XmlTag? {
    TODO("not implemented")
  }

  override fun getPrevSiblingInTag(): XmlTagChild? {
    TODO("not implemented")
  }

  override fun getNamespacePrefix(): String {
    TODO("not implemented")
  }

  override fun getPrefixByNamespace(namespace: String?): String? {
    TODO("not implemented")
  }

  override fun getLocalNamespaceDeclarations(): MutableMap<String, String> {
    TODO("not implemented")
  }

  override fun getNextSiblingInTag(): XmlTagChild? {
    TODO("not implemented")
  }

  override fun getValue(): XmlTagValue {
    TODO("not implemented")
  }

  override fun getLocalName(): String = ""

  override fun getSubTags(): Array<XmlTag> = emptyArray()

  override fun getAttributeValue(name: String?, namespace: String?): String? = null

  override fun getAttributeValue(qname: String?): String? = null

  override fun knownNamespaces(): Array<String> {
    TODO("not implemented")
  }

  override fun getNamespace(): String {
    TODO("not implemented")
  }

  override fun setAttribute(name: String?, namespace: String?, value: String?): XmlAttribute {
    TODO("not implemented")
  }

  override fun setAttribute(qname: String?, value: String?): XmlAttribute {
    TODO("not implemented")
  }

  override fun getMetaData(): PsiMetaData? {
    TODO("not implemented")
  }

  override fun addSubTag(subTag: XmlTag?, first: Boolean): XmlTag {
    TODO("not implemented")
  }

  override fun hasNamespaceDeclarations(): Boolean {
    TODO("not implemented")
  }
}

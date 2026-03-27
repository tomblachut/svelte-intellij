package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlTagDelegate
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.impl.source.xml.XmlTagDelegate
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlUtil
import dev.blachut.svelte.lang.codeInsight.SvelteComponentResolution
import dev.blachut.svelte.lang.codeInsight.getNamespacedComponentNavigation
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag
import dev.blachut.svelte.lang.psi.blocks.processConstTagDeclarations

// Check XmlTagImpl.createDelegate && HtmlTagDelegate if something breaks. Esp. HtmlTagDelegate.findSubTags
class SvelteHtmlTag : XmlTagImpl(SvelteHtmlElementTypes.SVELTE_HTML_TAG), HtmlTag {
  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement,
  ): Boolean {
    if (!processConstTagDeclarations(processor, state, lastParent, place)) {
      return false
    }

    for (attribute in attributes) {
      if (!attribute.processDeclarations(processor, state, lastParent, place)) {
        return false
      }
    }

    return true
  }

  override fun isCaseSensitive(): Boolean {
    return true
  }

  override fun getRealNs(value: String?): String? {
    return if (XmlUtil.XHTML_URI == value) XmlUtil.HTML_URI else value
  }

  override fun getParentTag(): XmlTag? {
    return PsiTreeUtil.getParentOfType(this, XmlTag::class.java)
  }

  // Copied from HTML
  override fun getNamespaceByPrefix(prefix: String): String {
    val xmlNamespace = super.getNamespaceByPrefix(prefix)
    if (prefix.isNotEmpty()) {
      return xmlNamespace
    }
    return if (xmlNamespace.isEmpty() || xmlNamespace == XmlUtil.XHTML_URI) {
      XmlUtil.HTML_URI
    }
    else xmlNamespace
    // ex.: mathML and SVG namespaces can be used inside html file
  }

  override fun getNamespacePrefix(): String {
    val tagName = name
    if (isSvelteNamespacedComponentTag(tagName)) {
      val lastDot = tagName.lastIndexOf('.')
      if (lastDot > 0) return tagName.substring(0, lastDot)
    }
    return super.getNamespacePrefix()
  }

  override fun createDelegate(): XmlTagDelegate {
    return SvelteHtmlTagDelegate()
  }

  override fun toString(): String {
    return "SvelteHtmlTag: $name"
  }

  private inner class SvelteHtmlTagDelegate : HtmlTagDelegate(this@SvelteHtmlTag) {
    override fun getNamespacePrefix(name: String): String {
      if (isSvelteNamespacedComponentTag(name)) {
        val dotIndex = name.lastIndexOf('.')
        if (dotIndex > 0) return name.substring(0, dotIndex)
      }
      return super.getNamespacePrefix(name)
    }

    override fun createPrefixReferences(
      startTagName: ASTNode,
      prefix: String,
      tagRef: TagNameReference?,
    ): Collection<PsiReference> {
      if (!isSvelteNamespacedComponentTag(myTag.name)) {
        return super.createPrefixReferences(startTagName, prefix, tagRef)
      }

      val nameOffsetInTag = startTagName.startOffset - myTag.node.startOffset
      val nameElement = startTagName.psi
      val references = mutableListOf<PsiReference>()
      var currentOffset = nameOffsetInTag
      val segments = prefix.split('.')

      for ((index, segment) in segments.withIndex()) {
        val segmentRange = TextRange(currentOffset, currentOffset + segment.length)
        val offsetInNameNode = currentOffset - nameOffsetInTag
        references.add(SvelteNamespacePrefixReference(myTag as SvelteHtmlTag, segmentRange, nameElement, offsetInNameNode, segments, index))
        currentOffset += segment.length + 1 // +1 for the dot
      }

      return references
    }

    override fun deleteChildInternalSuper(child: ASTNode) {
      this@SvelteHtmlTag.deleteChildInternalSuper(child)
    }

    override fun addInternalSuper(first: TreeElement, last: ASTNode, anchor: ASTNode?, before: Boolean?): TreeElement {
      return this@SvelteHtmlTag.addInternalSuper(first, last, anchor, before)
    }
  }
}

/**
 * Reference for a namespace prefix segment of a namespaced Svelte component tag.
 * E.g. for `<Forms.Button.Label />`, creates references for `Forms` (index 0) and `Button` (index 1).
 */
private class SvelteNamespacePrefixReference(
  tag: SvelteHtmlTag,
  rangeInElement: TextRange,
  private val nameNode: PsiElement,
  private val offsetInNameNode: Int,
  private val allSegments: List<String>,
  private val segmentIndex: Int,
) : PsiReferenceBase<SvelteHtmlTag>(tag, rangeInElement, true) {
  override fun resolve(): PsiElement? {
    val result = SvelteComponentResolution.resolveSegments(
      element, allSegments, segmentIndex, false
    ).firstOrNull()
    if (result != null) return result

    // LSP fallback
    val targets = getNamespacedComponentNavigation(element.project, nameNode, offsetInNameNode)
    return targets.firstOrNull()
  }

  override fun handleElementRename(newElementName: String): PsiElement {
    val tag = element
    val segments = tag.name.split('.').toMutableList()
    if (segmentIndex >= segments.size) return tag
    segments[segmentIndex] = newElementName
    tag.setName(segments.joinToString("."))
    return tag
  }
}

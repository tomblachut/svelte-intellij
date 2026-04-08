package dev.blachut.svelte.lang.css

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import dev.blachut.svelte.lang.compatibility.hasChildMarkupExpression
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteCssExpressionErrorFilter : HighlightErrorFilter() {
  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    return !isSvelteExpressionSpecialCase(element) && !isSvelteComponentStyleProp(element)
  }

  private fun isSvelteExpressionSpecialCase(element: PsiErrorElement): Boolean {
    if (element.parent !is CssElement) return false
    val ancestor = element.parentOfType<XmlAttributeValue>() ?: return false
    return hasChildMarkupExpression(ancestor)
  }

  private fun isSvelteComponentStyleProp(element: PsiErrorElement): Boolean {
    if (element.parent !is CssElement) return false
    val attrValue = element.parentOfType<XmlAttributeValue>() ?: return false
    val attr = attrValue.parent as? XmlAttribute ?: return false
    if (attr.name != "style") return false
    val tag = attr.parent ?: return false
    return tag.containingFile is SvelteHtmlFile && isSvelteComponentTag(tag.name)
  }
}
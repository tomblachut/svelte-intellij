package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.CssSelectorSuffixType
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

private val WHITESPACE_REGEX = "\\s+".toRegex()

internal class SvelteCssUsagesProvider : CssClassOrIdReferenceBasedUsagesProvider() {
  override fun isUsage(selectorSuffix: CssSelectorSuffix, candidate: PsiElement, offsetInCandidate: Int): Boolean {
    if (candidate is XmlAttributeValue && isSvelteElementClassOrId(candidate)) {
      val selectorName = selectorSuffix.name ?: return false
      val suffixType = selectorSuffix.type
      val attribute = candidate.parent as? XmlAttribute ?: return false
      val attrName = attribute.name
      if (suffixType == CssSelectorSuffixType.CLASS && attrName == "class") {
        // class attribute may contain multiple classes separated by whitespace, e.g. class="foo bar baz"
        return candidate.value.split(WHITESPACE_REGEX).any { it == selectorName }
      }
      if (suffixType == CssSelectorSuffixType.ID && attrName == "id") {
        return candidate.value == selectorName
      }
      return false
    }
    return super.isUsage(selectorSuffix, candidate, offsetInCandidate)
  }

  override fun acceptElement(candidate: PsiElement): Boolean {
    return candidate is SvelteHtmlAttribute
  }

  private fun isSvelteElementClassOrId(value: XmlAttributeValue): Boolean {
    val attribute = value.parent as? XmlAttribute ?: return false
    val tag = attribute.parent ?: return false
    return tag.name == "svelte:element" && (attribute.name == "class" || attribute.name == "id")
  }
}

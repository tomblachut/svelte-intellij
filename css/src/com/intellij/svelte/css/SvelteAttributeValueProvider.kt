package com.intellij.svelte.css

import com.intellij.html.impl.providers.HtmlAttributeValueProvider
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.javascript.backend.css.polySymbols.CssClassListInJSLiteralInHtmlAttributeScope.Companion.getClassesFromEmbeddedContent
import com.intellij.xml.util.getCustomHtmlClassAttributeValue
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.SmartList
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.directives.SvelteDirectiveTypes
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

/**
 * Required for class directive and class expression CSS references to work.
 */
internal class SvelteAttributeValueProvider : HtmlAttributeValueProvider() {

  override fun getCustomAttributeValues(tag: XmlTag, attributeName: String): String? {
    if (tag !is SvelteHtmlTag) return null

    if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attributeName, ignoreCase = true)
        && isSvelteClassAttributeTag(tag)) {
      return getCustomHtmlClassAttributeValue(tag) { attribute ->
        if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attribute.name, ignoreCase = true)) {
          getSvelteClassAttributeValue(attribute)
        }
        else {
          getSvelteClassDirectiveName(attribute)
        }
      }
    }

    val attribute = tag.getAttribute(attributeName) ?: return null
    return getSvelteClassDirectiveName(attribute)
  }
}

private fun getSvelteClassAttributeValue(attribute: XmlAttribute): String? {
  val value = attribute.valueElement ?: return null
  val result = SmartList<String>()
  var hasExpression = false
  for (child in value.children) {
    if (child.node.elementType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
      result.add(child.text)
    }
    else {
      val embeddedContent = PsiTreeUtil.findChildOfType(child, JSEmbeddedContent::class.java)
      if (embeddedContent != null) {
        hasExpression = true
        getClassesFromEmbeddedContent(embeddedContent).trim().takeIf { it.isNotEmpty() }?.let(result::add)
      }
    }
  }
  return if (hasExpression) result.joinToString(" ") else null
}

private fun getSvelteClassDirectiveName(attribute: XmlAttribute): String? {
  if (attribute !is SvelteHtmlAttribute) return null
  val directive = attribute.directive ?: return null
  if (directive.directiveType == SvelteDirectiveTypes.CLASS) {
    return directive.specifiers[0].text
  }
  return null
}

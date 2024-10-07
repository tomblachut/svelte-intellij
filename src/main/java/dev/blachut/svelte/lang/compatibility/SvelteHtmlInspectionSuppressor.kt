package dev.blachut.svelte.lang.compatibility

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownAnchorTargetInspection
import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.HtmlWrongAttributeValueInspection
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElement
import com.intellij.psi.css.inspections.invalid.CssUnresolvedCustomPropertyInspection
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.equalsName
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isSvelteContext

class SvelteHtmlInspectionSuppressor : DefaultXmlSuppressionProvider() {
  private val scriptAttributes = listOf("context", "generics", "module")
  private val styleAttributes = listOf("src", "global")
  private val legacyAAttributes = listOf("sapper:noscroll", "sapper:prefetch") // not descriptors, since we don't want completions

  override fun isProviderAvailable(file: PsiFile): Boolean {
    return isSvelteContext(file)
  }

  override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
    if (inspectionId.equalsName<XmlUnboundNsPrefixInspection>()) {
      return true
    }

    if (inspectionId.equalsName<HtmlUnknownTargetInspection>() || inspectionId.equalsName<HtmlUnknownAnchorTargetInspection>()) {
      return true
    }

    if (inspectionId.equalsName<HtmlUnknownAttributeInspection>()) {
      val attribute = element.parent
      if (attribute is XmlAttribute) {
        if (SvelteDirectiveUtil.directivePrefixes.contains(attribute.namespacePrefix)
            || "style" == attribute.namespacePrefix) return true

        // TODO refactor into proper descriptors
        if (attribute.parent.name == "script" && scriptAttributes.contains(attribute.name)) return true
        if (attribute.parent.name == "style" && styleAttributes.contains(attribute.name)) return true
        if (attribute.parent.name == "a" && legacyAAttributes.contains(attribute.name)) return true
      }
    }

    if (inspectionId.equalsName<HtmlUnknownTagInspection>()) {
      if (isSvelteComponentTag(element.text)) {
        return true
      }
    }

    if (inspectionId.equalsName<HtmlWrongAttributeValueInspection>()) {
      return element is XmlAttributeValue && hasChildMarkupExpression(element)
    }

    if (inspectionId.equalsName<XmlInvalidIdInspection>()) {
      return true // could try to limit it with hasChildMarkupExpression(element) as a next step
    }

    if (inspectionId.equalsName<XmlDuplicatedIdInspection>()) {
      return true // we'd need to use control flow analysis in XmlDuplicatedIdInspection for if blocks
    }

    if (inspectionId.equalsName<CssUnresolvedCustomPropertyInspection>()) { // WEB-60171
      return true // todo rewrite inline style parser to handle expressions, e.g.: style="--myColor: {myColor}; --opacity: {bgOpacity};"
    }

    if (element is CssElement && inspectionId.lowercase().startsWith("css") /* dangerous */) {
      return element.parentOfType<XmlAttributeValue>() != null
    }

    return super.isSuppressedFor(element, inspectionId)
  }
}

fun hasChildMarkupExpression(attributeValue: XmlAttributeValue): Boolean {
  // TODO Replace ASTWrapperPsiElement with dedicated expression type
  return PsiTreeUtil.getChildOfType(attributeValue, ASTWrapperPsiElement::class.java) != null
}
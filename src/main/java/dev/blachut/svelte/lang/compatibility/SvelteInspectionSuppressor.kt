package dev.blachut.svelte.lang.compatibility

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.inspection.JSUnusedAssignmentInspection
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.ecma6.impl.JSXXmlLiteralExpressionImpl
import com.intellij.lang.typescript.inspection.TypeScriptMissingConfigOptionInspection
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.control.UnnecessaryLabelJSInspection
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.equalsName
import dev.blachut.svelte.lang.psi.*

class SvelteInspectionSuppressor : InspectionSuppressor {
  override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
    if (element.containingFile !is SvelteHtmlFile) return false

    if (inspectionId.equalsName<UnnecessaryLabelJSInspection>()) {
      return element.textMatches(SvelteReactiveDeclarationsUtil.REACTIVE_LABEL)
    }
    if (inspectionId.equalsName<BadExpressionStatementJSInspection>()) {
      return true
    }
    if (inspectionId.equalsName<JSConstantReassignmentInspection>()) {
      val parent = element.parent
      if (parent is SvelteJSReferenceExpression && parent.isSubscribedReference) {
        // TODO check if store is writable
        return true
      }
    }
    if (inspectionId.equalsName<CommaExpressionJSInspection>()) {
      val parent = element.parents(false).first { it is JSEmbeddedContent || it is JSStatement }
      if (parent is JSEmbeddedContent &&
          parent.elementType == SvelteJSLazyElementTypes.CONTENT_EXPRESSION &&
          parent.children.any { it.node.elementType == SvelteTokenTypes.DEBUG_KEYWORD }) {
        return true
      }
    }
    if (inspectionId.equalsName<SillyAssignmentJSInspection>()) {
      // TODO check if resolved variable is declared directly in instance script
      return true
    }
    if (inspectionId.equalsName<PointlessBooleanExpressionJSInspection>()) {
      // TODO check if reference expression resolves to prop
      return true
    }
    if (inspectionId.equalsName<TypeScriptMissingConfigOptionInspection>()) {
      // JSXXmlLiteralExpressionImpl are invalid in Svelte anyway
      if (element is JSXXmlLiteralExpressionImpl) return true
    }
    if (inspectionId.equalsName<JSUnusedAssignmentInspection>()) {
      return true; // props + not yet isolated modifications from reactive statements WEB-61576
    }

    return false
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}

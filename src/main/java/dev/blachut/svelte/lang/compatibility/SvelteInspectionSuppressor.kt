package dev.blachut.svelte.lang.compatibility

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.inspection.JSObjectNullOrUndefinedInspection
import com.intellij.lang.javascript.inspection.JSUnusedAssignmentInspection
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.modules.TypeScriptCheckImportInspection
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.impl.JSXXmlLiteralExpressionImpl
import com.intellij.lang.typescript.inspection.TypeScriptMissingConfigOptionInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.util.parents
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.control.UnnecessaryLabelJSInspection
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.equalsName
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression
import dev.blachut.svelte.lang.psi.SvelteTokenTypes
import dev.blachut.svelte.lang.service.settings.tryRecheckResolveResults

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
      if (parent is SvelteJSReferenceExpression && parent.isStoreSubscription()) {
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
      return true // props + not yet isolated modifications from reactive statements WEB-61576
    }
    if (inspectionId.equalsName<JSObjectNullOrUndefinedInspection>()) {
      return true // not yet isolated modifications from reactive statements WEB-62551
    }
    if (inspectionId.equalsName<JSUnresolvedReferenceInspection>() || inspectionId.equalsName<TypeScriptUnresolvedReferenceInspection>()) {
      if (element.parent is JSReferenceExpression
          && element
            .parentOfTypes(JSObjectLiteralExpression::class, JSArrayLiteralExpression::class)
            ?.parentOfType<JSAssignmentExpression>()
            ?.parentOfType<JSLabeledStatement>()
            ?.takeIf { it.label == SvelteReactiveDeclarationsUtil.REACTIVE_LABEL } != null) {
        return true // likely suppresses too much
      }
    }
    if (inspectionId.equalsName<JSUnresolvedReferenceInspection>()) {
      // reactive declaration references
      val referenceExpression = element.parent as? JSReferenceExpression
      if (referenceExpression != null
          && referenceExpression.qualifier == null
          && referenceExpression.multiResolve(false).isNotEmpty()) {
        return true;
      }
    }
    if (inspectionId.equalsName<JSUnresolvedReferenceInspection>()
        || inspectionId.equalsName<TypeScriptUnresolvedReferenceInspection>()
        || inspectionId.equalsName<TypeScriptCheckImportInspection>()) {
      // destructured reactive declaration references, etc.
      val referenceExpression = element.parent as? JSPsiReferenceElement
      return referenceExpression != null
             && tryRecheckResolveResults(referenceExpression)
    }
    if (inspectionId.equalsName<JSUndeclaredVariableInspection>()) { // WEB-63611
      return true
    }

    return false
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}

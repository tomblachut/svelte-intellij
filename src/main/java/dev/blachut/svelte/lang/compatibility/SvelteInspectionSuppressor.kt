package dev.blachut.svelte.lang.compatibility

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.psi.PsiElement
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.control.UnnecessaryLabelJSInspection
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.equalsName
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (element.containingFile is SvelteHtmlFile) {
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
            if (inspectionId.equalsName<SillyAssignmentJSInspection>()) {
                // TODO check if resolved variable is declared directly in instance script
                return true
            }
            if (inspectionId.equalsName<PointlessBooleanExpressionJSInspection>()) {
                // TODO check if reference expression resolves to prop
                return true
            }
        }

        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}

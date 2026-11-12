package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.HtmlWrongAttributeValueInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSAnnotatorInspection
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection
import com.intellij.lang.javascript.inspections.JSClosureCompilerSyntaxInspection
import com.intellij.lang.javascript.inspections.JSCommentMatchesSignatureInspection
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.lang.javascript.inspections.JSFileReferencesInspection
import com.intellij.lang.javascript.inspections.JSIncompatibleTypesComparisonInspection
import com.intellij.lang.javascript.inspections.JSLastCommaInArrayLiteralInspection
import com.intellij.lang.javascript.inspections.JSLastCommaInObjectLiteralInspection
import com.intellij.lang.javascript.inspections.JSObjectNullOrUndefinedInspection
import com.intellij.lang.javascript.inspections.JSPotentiallyInvalidConstructorUsageInspection
import com.intellij.lang.javascript.inspections.JSPotentiallyInvalidTargetOfIndexedPropertyAccess
import com.intellij.lang.javascript.inspections.JSPotentiallyInvalidUsageOfClassThisInspection
import com.intellij.lang.javascript.inspections.JSPotentiallyInvalidUsageOfThisInspection
import com.intellij.lang.javascript.inspections.JSReferencingMutableVariableFromClosureInspection
import com.intellij.lang.javascript.inspections.JSSuspiciousTypeGuardInspection
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection
import com.intellij.lang.javascript.inspections.JSUndefinedPropertyAssignmentInspection
import com.intellij.lang.javascript.inspections.JSUnnecessarySemicolonInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSUnusedAssignmentInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateJSDocInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.lang.javascript.inspections.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.TypeScriptDuplicateUnionOrIntersectionTypeInspection
import com.intellij.lang.typescript.inspections.TypeScriptMissingConfigOptionInspection
import com.intellij.lang.typescript.inspections.TypeScriptRedundantGenericTypeInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateGenericTypesInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.xml.util.CheckEmptyTagInspection
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.control.UnnecessaryLabelJSInspection
import com.sixrr.inspectjs.validity.UnreachableCodeJSInspection
import dev.blachut.svelte.lang.inspections.SvelteUnresolvedComponentInspection

fun configureDefaultLocalInspectionTools(): List<InspectionProfileEntry> {
  val l = mutableListOf<LocalInspectionTool>()
  l.add(TypeScriptMissingConfigOptionInspection())
  l.add(RequiredAttributesInspection())
  l.add(HtmlRequiredAltAttributeInspection())
  l.add(HtmlRequiredTitleElementInspection())
  l.add(JSConstantReassignmentInspection())
  l.add(ES6UnusedImportsInspection())
  l.add(JSUnresolvedReferenceInspection())
  l.add(JSValidateTypesInspection())
  l.add(JSIncompatibleTypesComparisonInspection())
  l.add(SillyAssignmentJSInspection())
  l.add(UnnecessaryLabelJSInspection())
  l.add(CommaExpressionJSInspection())
  val functionSignaturesInspection = JSCheckFunctionSignaturesInspection()
  functionSignaturesInspection.myCheckGuessedTypes = true
  l.add(functionSignaturesInspection)
  l.add(JSValidateJSDocInspection())
  l.add(JSUndeclaredVariableInspection())
  l.add(XmlDuplicatedIdInspection())
  l.add(XmlInvalidIdInspection())
  l.add(HtmlUnknownTagInspection())
  l.add(CheckEmptyTagInspection())
  l.add(HtmlUnknownBooleanAttributeInspection())
  l.add(HtmlUnknownAttributeInspection())
  l.add(HtmlWrongAttributeValueInspection())
  l.add(XmlUnboundNsPrefixInspection())
  l.add(JSUnusedLocalSymbolsInspection())
  l.add(JSPotentiallyInvalidConstructorUsageInspection())
  l.add(JSUnnecessarySemicolonInspection())
  l.add(JSLastCommaInArrayLiteralInspection())
  l.add(JSLastCommaInObjectLiteralInspection())
  l.add(JSReferencingMutableVariableFromClosureInspection())
  l.add(JSPotentiallyInvalidUsageOfThisInspection())
  l.add(JSPotentiallyInvalidUsageOfClassThisInspection())
  l.add(JSPotentiallyInvalidTargetOfIndexedPropertyAccess())
  l.add(JSUndefinedPropertyAssignmentInspection())
  l.add(JSClosureCompilerSyntaxInspection())
  l.add(JSCommentMatchesSignatureInspection())
  l.add(JSFileReferencesInspection())
  l.add(JSUnusedGlobalSymbolsInspection())
  l.add(JSAnnotatorInspection())
  l.add(JSUnusedAssignmentInspection())
  l.add(UnreachableCodeJSInspection())
  l.add(TypeScriptValidateTypesInspection())
  l.add(TypeScriptUnresolvedReferenceInspection())
  l.add(TypeScriptCheckImportInspection())
  l.add(TypeScriptDuplicateUnionOrIntersectionTypeInspection())
  l.add(PointlessBooleanExpressionJSInspection())
  l.add(TypeScriptValidateGenericTypesInspection())
  l.add(TypeScriptRedundantGenericTypeInspection())

  // CF inspections
  l.add(JSSuspiciousTypeGuardInspection())
  l.add(JSObjectNullOrUndefinedInspection())
  l.add(SvelteUnresolvedComponentInspection())
  l.add(HtmlUnknownTargetInspection())
  return l
}

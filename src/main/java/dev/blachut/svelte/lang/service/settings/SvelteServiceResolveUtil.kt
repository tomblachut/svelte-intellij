package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.javascript.psi.JSPsiReferenceElement
import com.intellij.lang.typescript.compiler.TypeScriptCompilerService
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.ObjectUtils

enum class SvelteServiceReferenceResolveMode {
  OFF,
  BASIC,
  EXTENDED;

  companion object {
    val current: SvelteServiceReferenceResolveMode
      get() = runCatching { valueOf(Registry.get("svelte.service.reference.resolve.gtd").selectedOption!!.uppercase()) }.getOrDefault(OFF)
  }
}

fun tryRecheckResolveResults(expression: JSPsiReferenceElement): Boolean {
  if (SvelteServiceReferenceResolveMode.current == SvelteServiceReferenceResolveMode.OFF) return false

  if (expression.multiResolve(false).isNotEmpty()) return true
  return getServiceNavigationTargets(expression).isNullOrEmpty().not()
}

private fun getServiceNavigationTargets(expression: JSPsiReferenceElement): Array<PsiElement>? {
  val service = TypeScriptService.getForFile(expression.project, expression.containingFile.virtualFile)
  val document = PsiDocumentManager.getInstance(expression.project).getDocument(expression.containingFile)
  if (document == null || service == null) return null
  var identifier = TypeScriptCompilerService.adjustIntoIdentifier(expression)
  if (identifier is JSPsiReferenceElement) { // todo move to adjustIntoIdentifier, and/or handle that inside getNavigationFor
    identifier = ObjectUtils.coalesce(identifier.referenceNameElement, identifier)
  }
  return service.getNavigationFor(document, identifier)
}
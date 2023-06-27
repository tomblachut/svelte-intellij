package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.typescript.compiler.TypeScriptCompilerService
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

enum class SvelteServiceReferenceResolveMode {
  OFF,
  BASIC,
  EXTENDED;

  companion object {
    val current: SvelteServiceReferenceResolveMode
      get() = runCatching { valueOf(Registry.get("svelte.service.reference.resolve.gtd").selectedOption!!.uppercase()) }.getOrDefault(OFF)
  }
}

fun tryRecheckResolveResults(expression: JSReferenceExpression): Boolean {
  if (SvelteServiceReferenceResolveMode.current == SvelteServiceReferenceResolveMode.OFF) return false

  if (expression.multiResolve(false).isNotEmpty()) return true
  return getServiceNavigationTargets(expression).isNullOrEmpty().not()
}

private fun getServiceNavigationTargets(expression: JSReferenceExpression): Array<PsiElement>? {
  val service = TypeScriptService.getForFile(expression.project, expression.containingFile.virtualFile)
  val document = PsiDocumentManager.getInstance(expression.project).getDocument(expression.containingFile)
  if (document == null || service == null) return null
  val identifier = TypeScriptCompilerService.adjustIntoIdentifier(expression)
  return service.getNavigationFor(document, identifier)
}
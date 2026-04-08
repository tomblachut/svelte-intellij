package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.isSvelteProjectContext
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteKitImplicitUsageProvider : ImplicitUsageProvider {
  private val kitPageFiles = setOf("+page", "+layout", "+page.server", "+layout.server", "+server")
  private val kitServerClientHookFunctions = setOf("handle", "handleFetch", "handleError", "handleValidationError", "init")
  private val kitServerClientHookFiles = setOf("hooks.server", "hooks.client")
  private val kitUniversalHookFunctions = setOf("reroute", "transport")
  private val kitUniversalHookFiles = setOf("hooks")

  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (!isSvelteProjectContext(element)) return false

    if (element !is JSPsiElementBase) return false
    return isSvelteKitRouteExport(element)
           || isSvelteKitParamMatchFunction(element)
           || isSvelteKitHookFunction(element)
           || isSvelteKitSnapshotObjectProperty(element)
  }

  private fun isSvelteKitRouteExport(element: JSPsiElementBase): Boolean {
    // leave the actual export name to Svelte LSP; otherwise we'll get two problems reported on one element
    return isSvelteKitRouteFile(element.containingFile) && element.isExported && element.name?.startsWith('_') == false
  }

  private fun isSvelteKitParamMatchFunction(element: JSPsiElementBase): Boolean {
    return element.name == "match" && element.isExported && element.containingFile?.virtualFile?.parent?.name == "params"
  }

  private fun isSvelteKitHookFunction(element: JSPsiElementBase): Boolean {
    if (!element.isExported) return false
    val fileName = element.containingFile?.virtualFile?.nameWithoutExtension ?: return false
    return (element.name in kitServerClientHookFunctions && fileName in kitServerClientHookFiles)
           || (element.name in kitUniversalHookFunctions && fileName in kitUniversalHookFiles)
  }

  private fun isSvelteKitSnapshotObjectProperty(element: JSPsiElementBase): Boolean {
    val file = element.containingFile
    if (element !is JSProperty && file !is SvelteHtmlFile) return false
    if (!isSvelteKitRouteFile(file)) return false
    val parent = element.parentOfType<JSVariable>() ?: return false
    return parent.isExported && parent.name == "snapshot" // could also check if we're in a non-module script
  }

  private fun isSvelteKitRouteFile(file: PsiFile?): Boolean {
    val fileName = file?.virtualFile?.nameWithoutExtension
    val coreName = fileName?.split('@')?.firstOrNull()
    return coreName in kitPageFiles
  }

  override fun isImplicitRead(element: PsiElement) = false
  override fun isImplicitWrite(element: PsiElement) = false
}

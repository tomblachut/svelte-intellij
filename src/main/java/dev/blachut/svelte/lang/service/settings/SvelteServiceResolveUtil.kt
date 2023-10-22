package dev.blachut.svelte.lang.service.settings

import com.intellij.lang.javascript.psi.JSPsiReferenceElement
import com.intellij.lang.typescript.compiler.getServiceNavigationTargets
import com.intellij.openapi.util.registry.Registry

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

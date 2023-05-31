package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.findAncestorScript
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

class SvelteAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {
  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    val parentScript = findAncestorScript(place)
    if (parentScript != null) {
      // inside module or instance script
      return getJsEmbeddedContent(parentScript)
    }

    // inside Svelte expression
    val containingFile = place.containingFile as? SvelteHtmlFile ?: return null

    return prepareInstanceScriptContent(containingFile)
  }
}

package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.typescript.getResultsFromService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.isSvelteContext

/**
 * Meant to supersede ALL GotoDeclarationHandlers for Svelte files, since TypeScriptGotoDeclarationHandler is executed too late.
 */
class SvelteGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    sourceElement ?: return null
    if (!isSvelteContext(sourceElement)) return null
    return getResultsFromService(editor.project ?: return null, sourceElement, editor)
  }
}
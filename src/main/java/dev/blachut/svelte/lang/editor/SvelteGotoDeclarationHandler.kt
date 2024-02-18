package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.typescript.TypeScriptServiceGotoDeclarationHandler
import com.intellij.lang.typescript.getNavigationFromService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.isSvelteContext

/**
 * Meant to supersede ALL GotoDeclarationHandlers for Svelte files, since [TypeScriptServiceGotoDeclarationHandler] is executed too late.
 */
class SvelteGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    sourceElement ?: return null
    if (!isSvelteContext(sourceElement)) return null
    if (JSDocumentationUtils.getOriginalElementOrParentIfLeaf(sourceElement) is JSElement) {
      // pass to normal resolve and then to TypeScriptGotoDeclarationHandler
      return null
    }
    val project = editor.project ?: return null
    return getNavigationFromService(project, sourceElement, editor)
  }
}
package dev.blachut.svelte.lang.editor

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

/**
 * Called by [com.intellij.codeInsight.editorActions.wordSelection.WordSelectioner] to prevent default behavior
 *
 * Similar to [org.coffeescript.editor.CoffeeScriptSelectionFilter] and their `@references`.
 */
class SvelteSubscribedReferenceSelectionFilter : Condition<PsiElement> {
  override fun value(e: PsiElement): Boolean {
    return !isSvelteSubscribedReferenceIdentifier(e)
  }
}

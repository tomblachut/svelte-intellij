package dev.blachut.svelte.lang.css

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

/**
 * Optional CSS integration for the Svelte plugin.
 *
 * The implementation lives in the `intellij.svelte.css` content module and is loaded only when the CSS plugin is
 * available, so the Svelte plugin does not hard-require CSS.
 */
interface SvelteCssSupport {
  /**
   * Reference for a `class:` directive specifier pointing at a CSS class, or `null` when it cannot be resolved.
   */
  fun getClassReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange): PsiReference?

  /**
   * Adds CSS class name completions for a `class:` directive specifier.
   */
  fun addClassCompletions(attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet)

  /**
   * Whether the given element is a CSS PSI element.
   */
  fun isCssElement(element: PsiElement): Boolean

  /**
   * Languages of the registered CSS dialects (e.g. for `<style lang="...">`).
   */
  fun getStyleDialectLanguages(): List<Language>

  companion object {
    private val EP = ExtensionPointName.create<SvelteCssSupport>("com.intellij.svelte.cssSupport")

    fun getClassReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange): PsiReference? =
      EP.extensionList.firstOrNull()?.getClassReference(attribute, rangeInElement)

    fun addClassCompletions(attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) {
      EP.extensionList.firstOrNull()?.addClassCompletions(attribute, parameters, result)
    }

    fun isCssElement(element: PsiElement): Boolean =
      EP.extensionList.any { it.isCssElement(element) }

    fun getStyleDialectLanguages(): List<Language> =
      EP.extensionList.firstOrNull()?.getStyleDialectLanguages() ?: emptyList()
  }
}

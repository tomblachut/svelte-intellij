package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.psi.xml.XmlChildRole
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag

object SvelteComponentResolution {

  fun resolveTagOrComponent(tag: XmlTag, incompleteCode: Boolean): Array<ResolveResult> {
    val tagName = tag.name

    if (!isSvelteNamespacedComponentTag(tagName)) {
      return SvelteReactiveDeclarationsUtil.processLocalDeclarations(tag, tagName, incompleteCode)
    }

    // PSI first — returns intermediate elements (correct for rename and Find Usages)
    val psiResults = resolvePsi(tag, tagName, incompleteCode)
    if (psiResults.isNotEmpty() && psiResults.any { it.isValidResult }) return psiResults

    // LSP fallback
    val nameElement = XmlChildRole.START_TAG_NAME_FINDER.findChild(tag.node)?.psi
    if (nameElement != null) {
      val lspTargets = getNamespacedComponentNavigation(tag.project, nameElement, tagName.lastIndexOf('.') + 1)
      if (lspTargets.isNotEmpty()) return lspTargets.map { PsiElementResolveResult(it) }.toTypedArray()
    }

    return psiResults
  }

  internal fun resolveSegments(
    context: PsiElement,
    segments: List<String>,
    upToIndex: Int,
    incompleteCode: Boolean,
  ): Collection<PsiElement> {
    val namespaceResults = SvelteReactiveDeclarationsUtil.processLocalDeclarations(context, segments.first(), incompleteCode)
    if (namespaceResults.isEmpty()) return emptyList()
    if (upToIndex == 0) return JSResolveResult.toElements(namespaceResults)

    var currentElements: Collection<PsiElement> = JSResolveResult.toElements(namespaceResults)

    for (i in 1..upToIndex) {
      val expanded = ES6PsiUtil.expandElements(context, currentElements)
      if (expanded.isEmpty()) return emptyList()

      // Prefer direct JSProperty.value lookup for inline object literals.
      // Returns the actual JSProperty (renameable), unlike the synthetic
      // JSLocalImplicitElementImpl that getLocalElements may return via resolveFromType.
      val fromProperties = expanded.mapNotNull { findPropertyInExpanded(context, it, segments[i]) }
      if (fromProperties.isNotEmpty()) {
        currentElements = fromProperties
        continue
      }

      val members = ES6PsiUtil.createResolver(context).getLocalElements(segments[i], expanded)
      if (members.isEmpty()) return emptyList()

      currentElements = members
    }
    return currentElements
  }

  /**
   * Finds a named property in an expanded element. Handles two cases:
   * 1. Inline object literal: `export default { Label, Icon }` → JSProperty.value is JSObjectLiteralExpression
   * 2. Reference to another module: `export default { Button }` where Button is imported →
   *    JSProperty.value is JSReferenceExpression, follow it to the target module's default export
   */
  private fun findPropertyInExpanded(context: PsiElement, element: PsiElement, name: String): JSProperty? {
    if (element !is JSProperty) return null
    val value = element.value
    if (value is JSObjectLiteralExpression) {
      return value.findProperty(name)
    }
    if (value is JSReferenceExpression) {
      val target = value.resolve() ?: return null
      val targetExpanded = ES6PsiUtil.expandElements(context, listOf(target))
      for (exp in targetExpanded) {
        (exp as? JSObjectLiteralExpression)?.findProperty(name)?.let { return it }
      }
    }
    return null
  }

  private fun resolvePsi(tag: XmlTag, tagName: String, incompleteCode: Boolean): Array<ResolveResult> {
    val segments = tagName.split('.')
    val elements = resolveSegments(tag, segments, segments.lastIndex, incompleteCode)
    if (elements.isNotEmpty()) return elements.map { PsiElementResolveResult(it) }.toTypedArray()

    // Fallback: return namespace (first segment) results so callers can still use them
    return SvelteReactiveDeclarationsUtil.processLocalDeclarations(tag, segments.first(), incompleteCode)
  }
}

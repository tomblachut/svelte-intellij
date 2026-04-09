package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.xml.TagNameReference
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import dev.blachut.svelte.lang.service.SvelteLspTypeScriptService

class SvelteTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) :
  TagNameReference(nameElement, startTagFlag), PsiPolyVariantReference {

  override fun resolve(): PsiElement? {
    val results = this.multiResolve(false)
    return if (results.isNotEmpty()) results[0].element else null
  }

  override fun getPrefixIndex(name: String): Int = name.lastIndexOf('.')

  override fun prependNamespacePrefix(newElementName: String, namespacePrefix: String): String {
    return if (namespacePrefix.isNotEmpty()) "$namespacePrefix.$newElementName" else newElementName
  }

  override fun isReferenceTo(element: PsiElement): Boolean {
    val tag = tagElement ?: return false
    val tagName = tag.name
    if (isSvelteNamespacedComponentTag(tagName)) {
      val lastSegment = tagName.substringAfterLast('.')
      val elementName = (element as? JSElement)?.name
                        ?: (element as? PsiFile)?.virtualFile?.nameWithoutExtension
      if (elementName != null && elementName != lastSegment) return false
    }
    if (super.isReferenceTo(element)) return true
    // resolve() returns intermediate elements (JSProperty, ES6ImportedBinding) that may
    // reference the target through import chains of arbitrary depth (e.g., Forms.Button.Label).
    val resolved = resolve() ?: return false
    return resolvesToTarget(resolved, element)
  }

  /**
   * Follows the import/export chain from [resolved] to check if it ultimately references [target].
   * Handles: JSProperty → value.resolve() → ES6ImportedBinding → findReferencedElements() → PsiFile.
   * Recursion depth is bounded by the namespace nesting level (typically 2-3).
   */
  private fun resolvesToTarget(resolved: PsiElement, target: PsiElement): Boolean {
    if (target.manager.areElementsEquivalent(resolved, target)) return true
    if (resolved is ES6ImportedBinding) {
      return resolved.findReferencedElements().any { target.manager.areElementsEquivalent(it, target) }
    }
    if (resolved is JSProperty) {
      val value = resolved.value
      if (value is JSReferenceExpression) {
        val next = value.resolve() ?: return false
        return resolvesToTarget(next, target)
      }
    }
    return false
  }

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val resolver = ResolveCache.PolyVariantResolver<SvelteTagNameReference> { ref, incomplete ->
      val tag = ref.tagElement ?: return@PolyVariantResolver emptyArray()
      SvelteComponentResolution.resolveTagOrComponent(tag, incomplete)
    }

    return JSResolveUtil.resolve(element.containingFile, this, resolver, incompleteCode)
  }

  companion object {
    fun resolveComponentFile(tag: SvelteHtmlTag): SvelteHtmlFile? {
      if (isSvelteNamespacedComponentTag(tag.name)) return null
      val import = tag.reference?.resolve()
      if (import is ES6ImportedBinding && !import.isNamespaceImport) {
        val componentFile = import.findReferencedElements().firstOrNull()

        if (componentFile is SvelteHtmlFile) {
          return componentFile
        }
      }

      return null
    }
  }
}

internal fun getNamespacedComponentNavigation(
  project: com.intellij.openapi.project.Project,
  sourceElement: PsiElement,
  offsetInSourceElement: Int,
): Array<PsiElement> {
  val virtualFile = sourceElement.containingFile?.virtualFile ?: return PsiElement.EMPTY_ARRAY
  val service = TypeScriptServiceHolder.getForFile(project, virtualFile) ?: return PsiElement.EMPTY_ARRAY
  if (service !is SvelteLspTypeScriptService) return PsiElement.EMPTY_ARRAY
  val document = PsiDocumentManager.getInstance(project).getDocument(sourceElement.containingFile) ?: return PsiElement.EMPTY_ARRAY
  return service.getNavigationForNamespacedComponent(document, sourceElement, offsetInSourceElement)
}

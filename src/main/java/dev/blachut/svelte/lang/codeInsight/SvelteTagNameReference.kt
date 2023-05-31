package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.xml.TagNameReference
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

class SvelteTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) :
  TagNameReference(nameElement, startTagFlag), PsiPolyVariantReference {

  override fun resolve(): PsiElement? {
    val results = this.multiResolve(false)
    return if (results.isNotEmpty()) results[0].element else null
  }

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val resolver = ResolveCache.PolyVariantResolver<SvelteTagNameReference> { ref, incomplete ->
      val place = ref.tagElement ?: return@PolyVariantResolver emptyArray()
      val referenceName = place.name
      SvelteReactiveDeclarationsUtil.processLocalDeclarations(place, referenceName, incomplete)
    }

    return JSResolveUtil.resolve(element.containingFile, this, resolver, incompleteCode)
  }

  companion object {
    fun resolveComponentFile(tag: SvelteHtmlTag): SvelteHtmlFile? {
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

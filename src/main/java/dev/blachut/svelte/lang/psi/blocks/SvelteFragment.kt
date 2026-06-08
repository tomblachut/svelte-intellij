package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.util.XmlPsiUtil
import dev.blachut.svelte.lang.psi.ContentExpressionType
import dev.blachut.svelte.lang.psi.SveltePsiElement

class SvelteFragment(node: ASTNode) : SveltePsiElement(node), XmlElement {
  override fun processElements(processor: PsiElementProcessor<PsiElement>, place: PsiElement?): Boolean {
    return XmlPsiUtil.processXmlElements(this, processor, false)
  }

  override fun processDeclarations(processor: PsiScopeProcessor,
                                   state: ResolveState,
                                   lastParent: PsiElement?,
                                   place: PsiElement): Boolean {
    return processConstTagDeclarations(processor, state, lastParent, place)
  }
}

internal fun XmlElement.processConstTagDeclarations(processor: PsiScopeProcessor,
                                                    state: ResolveState,
                                                    lastParent: PsiElement?,
                                                    place: PsiElement,
                                                    skipLegacyAtConst: Boolean = false): Boolean {
  for (element in children) {
    if (element.elementType is ContentExpressionType) {
      // Legacy `{@const ...}` carries a leading `@` (JS:AT); the new bare `{const}`/`{let}` do not.
      // At scopes where `{@const}` is not valid (e.g. the file top level) it must stay non-resolving.
      if (skipLegacyAtConst && element.node.findChildByType(JSTokenTypes.AT) != null) continue

      val visitor = object : JSRecursiveWalkingElementVisitor() {
        var result = true

        override fun visitJSVarStatement(node: JSVarStatement) {
          if (!node.processDeclarations(processor, state, lastParent, place)) {
            result = false
            stopWalking()
          }
        }
      }
      element.acceptChildren(visitor)
      if (!visitor.result) {
        return false
      }
    }
  }

  return true
}

// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSSuppressionHolder
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.ecma6.TypeScriptImportStatement
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.psi.HintedReferenceHost
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.DelegatingScopeProcessor
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.stubs.SvelteGenericsExpressionContentStub

/**
 * PSI implementation for the generics attribute embedded content.
 */
class SvelteGenericsExpressionContentImpl :
  JSStubElementImpl<SvelteGenericsExpressionContentStub>,
  JSSuppressionHolder,
  SvelteGenericsExpressionContent,
  HintedReferenceHost,
  TypeScriptTypeParameterListOwner {

  constructor(node: ASTNode) : super(node)

  constructor(stub: SvelteGenericsExpressionContentStub, type: IElementType) : super(stub, type)

  override fun getLanguage(): Language = iElementType.language

  override fun getIElementType(): IElementType =
    super<JSStubElementImpl>.elementTypeImpl

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is JSElementVisitor) {
      visitor.visitJSEmbeddedContent(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun allowTopLevelThis(): Boolean = true

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getQuoteChar(): Char? = JSEmbeddedContentImpl.getQuoteChar(this)

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> = PsiReference.EMPTY_ARRAY

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean = false

  override fun toString(): String = super.toString() + "(${language.id})"

  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement,
  ): Boolean {
    // Process this element's own type parameters first so a generic parameter shadows a
    // same-named <script> body declaration, matching TypeScript scoping.
    if (!super.processDeclarations(processor, state, lastParent, place)) {
      return false
    }
    // This element holds the `generics` attribute value, a sibling of the enclosing <script> body
    // rather than a descendant, so the body's declarations are not in its scope chain. Bridge
    // sideways into the body - filtered, see HoistedImportsOnlyProcessor - mirroring the reverse
    // direction in SvelteJSEmbeddedContentImpl (and Vue's VueJSEmbeddedExpressionContentImpl).
    val scriptTag = PsiTreeUtil.getContextOfType(this, XmlTag::class.java)?.takeIf { HtmlUtil.isScriptTag(it) }
    val scriptBody = getJsEmbeddedContent(scriptTag)
    if (scriptBody != null && scriptBody !== this) {
      return scriptBody.processDeclarations(HoistedImportsOnlyProcessor(processor), state, lastParent, place)
    }
    return true
  }

  override fun getTypeParameterList(): TypeScriptTypeParameterList? {
    // Prefer the stub tree to avoid loading the AST; falls back to AST when no stub is available.
    return PsiTreeUtil.getStubChildOfType(this, TypeScriptTypeParameterList::class.java)
  }
}

/**
 * Forwards only the `<script>` body declarations that are visible from the `generics` attribute.
 *
 * svelte2tsx compiles the script to `function render<T extends Foo>() { ...body... }` and hoists
 * import declarations above that function, so imports are in scope for a constraint while
 * interfaces, type aliases and classes - which stay inside `render()` - are not. Forwarding those
 * too would let the IDE resolve code the Svelte compiler rejects with "Foo is not defined".
 */
private class HoistedImportsOnlyProcessor(delegate: PsiScopeProcessor) : DelegatingScopeProcessor(delegate) {
  override fun execute(element: PsiElement, state: ResolveState): Boolean =
    if (isHoistedImport(element)) super.execute(element, state) else true

  private fun isHoistedImport(element: PsiElement): Boolean =
    element is ES6ImportedBinding
    || element is ES6ImportSpecifier
    || element is ES6ImportSpecifierAlias
    || element is TypeScriptImportStatement
}

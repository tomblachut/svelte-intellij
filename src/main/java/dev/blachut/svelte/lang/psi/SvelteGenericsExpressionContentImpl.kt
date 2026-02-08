// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSSuppressionHolder
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
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
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
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
    return super.processDeclarations(processor, state, lastParent, place)
  }

  override fun getTypeParameterList(): TypeScriptTypeParameterList? {
    return PsiTreeUtil.getChildOfType(this, TypeScriptTypeParameterList::class.java)
  }
}

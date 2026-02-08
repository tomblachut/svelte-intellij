// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.psi.PsiElement

/**
 * Element type for the generics attribute embedded expression content.
 */
class SvelteGenericsExpressionContentElementType(
  debugName: String,
  private val language: Language,
) : JSElementType<SvelteGenericsExpressionContent>(debugName) {

  override fun getLanguage(): Language = language

  override fun construct(node: ASTNode): PsiElement {
    return SvelteGenericsExpressionContentImpl(node)
  }

  override fun toString(): String = "Svelte:$debugName"
}

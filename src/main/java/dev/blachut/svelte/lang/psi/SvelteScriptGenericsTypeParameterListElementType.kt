// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeParameterListImpl
import com.intellij.psi.PsiElement

/**
 * Element type for the type parameter list parsed from the generics attribute.
 */
class SvelteScriptGenericsTypeParameterListElementType : JSElementType<TypeScriptTypeParameterList>(
  "SCRIPT_GENERICS_TYPE_PARAMETER_LIST"
) {
  override fun construct(node: ASTNode): PsiElement {
    return TypeScriptTypeParameterListImpl(node)
  }

  override fun toString(): String = "Svelte:$debugName"
}

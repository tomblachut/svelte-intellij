// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptVariableImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.blocks.SvelteBranch

/**
 * TypeScript-aware variant of const tag variable for `{@const ...}` declarations.
 * Supports TypeScript type annotations like `{@const foo: Type = expr}`.
 */
class SvelteTSConstTagVariable(node: ASTNode) : TypeScriptVariableImpl(node) {
  override fun hasBlockScope(): Boolean = true

  override fun getDeclarationScope(): PsiElement? {
    return PsiTreeUtil.getContextOfType(this, SvelteBranch::class.java, XmlTag::class.java, PsiFile::class.java)
  }

  override fun isConst(): Boolean = true
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.blocks.SvelteBranch

class SvelteJSConstTagVariable(node: ASTNode) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node) {
    override fun hasBlockScope(): Boolean = true

    override fun getDeclarationScope(): PsiElement? {
        return PsiTreeUtil.getContextOfType(this, SvelteBranch::class.java, XmlTag::class.java, PsiFile::class.java)
    }

    override fun isConst(): Boolean = true
}

package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import dev.blachut.svelte.lang.psi.SveltePsiElement

class SvelteTagDependentExpression(node: ASTNode) : SveltePsiElement(node), JSElement

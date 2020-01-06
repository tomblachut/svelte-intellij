package dev.blachut.svelte.lang.parsing.html.psi

import com.intellij.lang.ASTNode
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl

sealed class SvelteBranch(node: ASTNode) : SveltePsiElementImpl(node)

abstract class SveltePrimaryBranch(node: ASTNode) : SvelteBranch(node)

abstract class SvelteAuxiliaryBranch(node: ASTNode) : SvelteBranch(node)

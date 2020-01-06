package dev.blachut.svelte.lang.parsing.html.psi

import com.intellij.lang.ASTNode
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl

abstract class SvelteBlock(node: ASTNode) : SveltePsiElementImpl(node)
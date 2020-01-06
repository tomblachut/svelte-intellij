package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode

class SvelteEndTag(node: ASTNode) : SveltePsiElementImpl(node) {
    override fun toString(): String {
        return super.toString() + "(" + node.elementType.toString() + ")"
    }
}

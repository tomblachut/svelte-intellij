package dev.blachut.svelte.lang.format

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock

class SvelteLeafBlock(node: ASTNode, private val indent: Indent? = null, wrap: Wrap? = null) :
    AbstractBlock(node, wrap, null) {
    override fun isLeaf(): Boolean = true

    override fun buildChildren(): MutableList<Block> = EMPTY

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getIndent(): Indent? = indent
}

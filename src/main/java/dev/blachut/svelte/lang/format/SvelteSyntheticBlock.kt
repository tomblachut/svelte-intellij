package dev.blachut.svelte.lang.format

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.formatter.xml.AbstractXmlBlock
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.tree.TokenSet

class SvelteSyntheticBlock(subBlocks: List<Block?>?, parent: Block?, indent: Indent?, policy: XmlFormattingPolicy?, childIndent: Indent?)
    : SyntheticBlock(subBlocks, parent, indent, policy, childIndent) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (child1 !is AbstractXmlBlock || child2 !is AbstractXmlBlock) {
            return null
        }
        val node1 = child1.node
        val node2 = child2.node

        val type1 = node1.elementType
        val type2 = node2.elementType

        if (BLOCK_KEYWORDS.contains(type1) && type2 !== JSTokenTypes.RBRACE) {
            return Spacing.createSpacing(1, 1, 0, myXmlFormattingPolicy.shouldKeepLineBreaks, myXmlFormattingPolicy.keepBlankLines)
        }

        return super.getSpacing(child1, child2)
    }
}

// TODO Add THEN_KEYWORD
val BLOCK_KEYWORDS = TokenSet.create(JSTokenTypes.IF_KEYWORD, JSTokenTypes.EACH_KEYWORD, JSTokenTypes.ELSE_KEYWORD, JSTokenTypes.AWAIT_KEYWORD, JSTokenTypes.CATCH_KEYWORD)

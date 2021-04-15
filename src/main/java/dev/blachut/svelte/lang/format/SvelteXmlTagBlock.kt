package dev.blachut.svelte.lang.format

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.formatter.xml.AbstractXmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteEndTag
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes

class SvelteXmlTagBlock(
    node: ASTNode?,
    wrap: Wrap?,
    alignment: Alignment?,
    policy: XmlFormattingPolicy?,
    indent: Indent?,
    preserveSpace: Boolean
) : SvelteXmlTagBlockBase(node, wrap, alignment, policy, indent, preserveSpace) {
    override fun canWrapTagEnd(tag: XmlTag): Boolean {
        if (SvelteHtmlPolicy.wrappingTags.contains(tag.name)) return true

        return super.canWrapTagEnd(tag)
    }

    override fun buildSvelteChildren(): List<Block> {
        val textWrap = Wrap.createWrap(AbstractXmlBlock.getWrapType(myXmlFormattingPolicy.getTextWrap(tag)), true)
        val results = ArrayList<Block>(3)

        var child = myNode.firstChildNode
        while (child != null) {
            if (!AbstractXmlBlock.containsWhiteSpacesOnly(child) && child.textLength > 0) {
                if (SvelteElementTypes.BRANCHES.contains(child.elementType)) {
                    val tag = child.firstChildNode
                    val fragment = child.lastChildNode

                    processTag(results, tag)
                    processFragment(results, fragment, textWrap)
                } else if (child.psi is SvelteEndTag) {
                    processTag(results, child)
                }
            }

            child = child.treeNext
        }

        return results
    }

    private fun processTag(results: ArrayList<Block>, tag: ASTNode) {
        val localResults = ArrayList<Block>(4)

        var child = tag.firstChildNode
        while (child != null) {
            if (!AbstractXmlBlock.containsWhiteSpacesOnly(child) && child.textLength > 0) {
                if (child.elementType === JSTokenTypes.LBRACE) {
                    val startTag = SvelteTagElementTypes.START_TAGS.contains(tag.elementType)
                    val wrap = if (startTag) Wrap.createWrap(WrapType.ALWAYS, true) else null
                    localResults.add(createSimpleChild(child, null, wrap, null, null))
                } else if (child.elementType === JSTokenTypes.RBRACE) {
                    localResults.add(createSimpleChild(child, Indent.getNoneIndent(), null, null, null))
                } else {
                    child = processChild(localResults, child, null, null, null)
                }
            }

            if (child != null) {
                child = child.treeNext
            }
        }

        // Same as XmlTagBlock.createTagDescriptionNode
        results.add(createSyntheticBlock(localResults, null))
    }

    private fun processFragment(results: ArrayList<Block>, fragment: ASTNode, textWrap: Wrap?) {
        val localResults = ArrayList<Block>(1)

        var child = fragment.firstChildNode
        while (child != null) {
            if (!AbstractXmlBlock.containsWhiteSpacesOnly(child) && child.textLength > 0) {
                val wrap = chooseWrap(child, null, null, textWrap)

                child = processChild(localResults, child, wrap, null, Indent.getNormalIndent())
            }

            if (child != null) {
                child = child.treeNext
            }
        }

        if (localResults.isNotEmpty()) {
            results.add(createSyntheticBlock(localResults, Indent.getNormalIndent()))
        }
    }

    override fun createSyntheticBlock(localResult: ArrayList<Block>?, childrenIndent: Indent?): Block {
        return SvelteSyntheticBlock(localResult, this, Indent.getNoneIndent(), myXmlFormattingPolicy, childrenIndent)
    }
}

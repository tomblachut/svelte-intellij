package dev.blachut.svelte.lang.format

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.xml.AbstractXmlBlock
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteEndTag
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock

class SvelteXmlBlock(
    node: ASTNode?,
    wrap: Wrap?,
    alignment: Alignment?,
    policy: XmlFormattingPolicy?,
    indent: Indent?,
    textRange: TextRange?,
    preserveSpace: Boolean
) : XmlBlock(node, wrap, alignment, policy, indent, textRange, preserveSpace) {
    override fun getTag(): XmlTag {
        return super.getTag() ?: getFakeTag(myNode)
    }

    override fun processSimpleChild(child: ASTNode, indent: Indent?, result: MutableList<in Block>, wrap: Wrap?, alignment: Alignment?) {
        if (isSvelteBlock(child)) {
            result.add(createTagBlock(child, indent ?: Indent.getNoneIndent(), wrap, alignment))
        } else {
            super.processSimpleChild(child, indent, result, wrap, alignment)
        }
    }

    override fun createSimpleChild(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlBlock {
        return createSimpleChild(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }

    override fun createTagBlock(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
        return createTagBlock(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }
}

open class SvelteXmlTagBlock(
    node: ASTNode?,
    wrap: Wrap?,
    alignment: Alignment?,
    policy: XmlFormattingPolicy?,
    indent: Indent?,
    preserveSpace: Boolean
) : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace) {
    override fun getTag(): XmlTag {
        return super.getTag() ?: getFakeTag(myNode)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (!isSvelteBlock(myNode)) {
            return super.getChildAttributes(newChildIndex)
        }

        return ChildAttributes(Indent.getNormalIndent(), null)
    }

    override fun getChildrenIndent(): Indent {
        if (!isSvelteBlock(myNode)) {
            return super.getChildrenIndent()
        }

        return Indent.getNormalIndent()
    }

    override fun buildChildren(): List<Block>? {
        if (!isSvelteBlock(myNode)) {
            return super.buildChildren()
        }

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
                    localResults.add(createSimpleChild(child, null, wrap, null))
                } else if (child.elementType === JSTokenTypes.RBRACE) {
                    localResults.add(createSimpleChild(child, Indent.getNoneIndent(), null, null))
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
                val wrap = chooseWrap(child, null,  null, textWrap)

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

    override fun processSimpleChild(child: ASTNode, indent: Indent?, result: MutableList<in Block>, wrap: Wrap?, alignment: Alignment?) {
        if (isSvelteBlock(child)) {
            result.add(createTagBlock(child, indent ?: Indent.getNoneIndent(), wrap, alignment))
        } else {
            super.processSimpleChild(child, indent, result, wrap, alignment)
        }
    }

    override fun createSimpleChild(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlBlock {
        return createSimpleChild(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }

    override fun createTagBlock(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
        return createTagBlock(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }

    override fun createSyntheticBlock(localResult: ArrayList<Block>?, childrenIndent: Indent?): Block {
        return SvelteSyntheticBlock(localResult, this, Indent.getNoneIndent(), myXmlFormattingPolicy, childrenIndent)
    }
}

private fun AbstractXmlBlock.createSimpleChild(xmlFormattingPolicy: XmlFormattingPolicy?, child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlBlock {
    return SvelteXmlBlock(child, wrap, alignment, xmlFormattingPolicy, indent, null, isPreserveSpace)
}

private fun AbstractXmlBlock.createTagBlock(xmlFormattingPolicy: XmlFormattingPolicy?, child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    val newIndent = indent ?: Indent.getNoneIndent()
    return SvelteXmlTagBlock(child, wrap, alignment, xmlFormattingPolicy, newIndent, isPreserveSpace)
}

private fun getFakeTag(node: ASTNode): XmlTag {
    val element = SourceTreeToPsiMap.treeElementToPsi(node)

    if (element is SvelteBlock) {
        return SvelteBlockFakeXmlTag(element)
    }

    throw AssertionError("Shouldn't happen")
}

private fun isSvelteBlock(child: ASTNode): Boolean {
    return child.psi is SvelteBlock
}

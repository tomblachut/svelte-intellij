package dev.blachut.svelte.lang.format

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.xml.AbstractXmlBlock
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes.CONTENT_EXPRESSION
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
    override fun processChild(
        result: MutableList<Block>,
        child: ASTNode,
        wrap: Wrap?,
        alignment: Alignment?,
        indent: Indent?
    ): ASTNode? {
        if (child.elementType === CONTENT_EXPRESSION) {
            result.add(SvelteExpressionBlock(child, indent, wrap, myXmlFormattingPolicy))
            return child
        }

        return super.processChild(result, child, wrap, alignment, indent)
    }

    override fun processSimpleChild(
        child: ASTNode,
        indent: Indent?,
        result: MutableList<in Block>,
        wrap: Wrap?,
        alignment: Alignment?
    ) {
        if (isSvelteBlock(child)) {
            result.add(createTagBlock(child, indent ?: Indent.getNoneIndent(), wrap, alignment))
        } else {
            super.processSimpleChild(child, indent, result, wrap, alignment)
        }
    }

    @Suppress("UnstableApiUsage")
    override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
        return createSimpleChild(myXmlFormattingPolicy, child, indent, wrap, alignment, range)
    }

    override fun createTagBlock(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
        return createTagBlock(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }
}

abstract class SvelteXmlTagBlockBase(
    node: ASTNode?,
    wrap: Wrap?,
    alignment: Alignment?,
    policy: XmlFormattingPolicy?,
    indent: Indent?,
    preserveSpace: Boolean
) : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace) {
    // start getTag related overrides
    override fun getTag(): XmlTag {
        return super.getTag() ?: getFakeTag(myNode)
    }

    private fun getFakeTag(node: ASTNode): XmlTag {
        val element = SourceTreeToPsiMap.treeElementToPsi(node)

        if (element is SvelteBlock) {
            return SvelteBlockFakeXmlTag(element)
        }

        throw AssertionError("getFakeTag used not for SvelteBlock")
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (isSvelteBlock(myNode)) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        return super.getChildAttributes(newChildIndex)
    }

    override fun getChildrenIndent(): Indent {
        if (isSvelteBlock(myNode)) {
            return Indent.getNormalIndent()
        }

        return super.getChildrenIndent()
    }
    // end getTag related overrides

    override fun buildChildren(): List<Block> {
        if (isSvelteBlock(myNode)) {
            return buildSvelteChildren()
        }

        return super.buildChildren()
    }

    abstract fun buildSvelteChildren(): List<Block>

    override fun processChild(
        result: MutableList<Block>,
        child: ASTNode,
        wrap: Wrap?,
        alignment: Alignment?,
        indent: Indent?
    ): ASTNode? {
        if (child.elementType === CONTENT_EXPRESSION) {
            result.add(SvelteExpressionBlock(child, indent, wrap, myXmlFormattingPolicy))
            return child
        }

        return super.processChild(result, child, wrap, alignment, indent)
    }

    override fun processSimpleChild(
        child: ASTNode,
        indent: Indent?,
        result: MutableList<in Block>,
        wrap: Wrap?,
        alignment: Alignment?
    ) {
        if (isSvelteBlock(child)) {
            result.add(createTagBlock(child, indent ?: Indent.getNoneIndent(), wrap, alignment))
        } else {
            super.processSimpleChild(child, indent, result, wrap, alignment)
        }
    }

    @Suppress("UnstableApiUsage")
    override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
        return createSimpleChild(myXmlFormattingPolicy, child, indent, wrap, alignment, range)
    }

    override fun createTagBlock(child: ASTNode?, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
        return createTagBlock(myXmlFormattingPolicy, child, indent, wrap, alignment)
    }
}

private fun AbstractXmlBlock.createSimpleChild(
    xmlFormattingPolicy: XmlFormattingPolicy,
    child: ASTNode,
    indent: Indent?,
    wrap: Wrap?,
    alignment: Alignment?,
    range: TextRange?
): XmlBlock {
    return SvelteXmlBlock(child, wrap, alignment, xmlFormattingPolicy, indent, range, isPreserveSpace)
}

private fun AbstractXmlBlock.createTagBlock(
    xmlFormattingPolicy: XmlFormattingPolicy?,
    child: ASTNode?,
    indent: Indent?,
    wrap: Wrap?,
    alignment: Alignment?
): XmlTagBlock {
    val newIndent = indent ?: Indent.getNoneIndent()
    return SvelteXmlTagBlock(child, wrap, alignment, xmlFormattingPolicy, newIndent, isPreserveSpace)
}

private fun isSvelteBlock(child: ASTNode): Boolean {
    return child.psi is SvelteBlock
}

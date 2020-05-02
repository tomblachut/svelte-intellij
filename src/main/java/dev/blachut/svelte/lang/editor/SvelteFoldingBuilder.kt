package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.xml.XmlFoldingBuilder
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlElement
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock
import dev.blachut.svelte.lang.psi.blocks.SvelteBranch

class SvelteFoldingBuilder : XmlFoldingBuilder(), DumbAware {
    override fun doAddForChildren(tag: XmlElement, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        for (child in tag.children) {
            if (child is SvelteBlock) {
                appendBlockDescriptors(child, descriptors, document)
            }
        }

        super.doAddForChildren(tag, descriptors, document)
    }

    private fun appendBlockDescriptors(block: SvelteBlock, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        if (isSingleLine(block, document)) {
            return
        }

        val endTag = block.endTag

        if (endTag != null) {
            // Following offsets ensure that we fold start and end tag together exactly like HTML does
            // E.g. {#if condition...}
            val foldingRangeStartOffset = block.startTag.textRange.endOffset - 1
            val foldingRangeEndOffset = endTag.textRange.endOffset - 1
            val range = TextRange(foldingRangeStartOffset, foldingRangeEndOffset)

            descriptors.add(FoldingDescriptor(block, range))
        }

        for (child in block.children) {
            if (child is SvelteBranch) {
                doAddForChildren(child.fragment, descriptors, document)
            }
        }
    }

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        if (node.psi is SvelteBlock) return "..."
        return super.getLanguagePlaceholderText(node, range)
    }

    private fun isSingleLine(element: PsiElement, document: Document): Boolean {
        val range = element.textRange
        return document.getLineNumber(range.startOffset) == document.getLineNumber(range.endOffset)
    }
}

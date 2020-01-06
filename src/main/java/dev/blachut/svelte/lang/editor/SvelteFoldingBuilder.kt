package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.parsing.html.psi.SvelteBlock
import java.util.*

class SvelteFoldingBuilder : FoldingBuilder, DumbAware {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        appendDescriptors(node.psi, descriptors, document)
        return descriptors.toTypedArray()
    }

    private fun appendDescriptors(block: PsiElement, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        if (isSingleLine(block, document)) {
            return
        }

        if (block is SvelteBlock) {
            val endTag = block.endTag

            if (endTag != null) {
                // Following offsets ensure that we fold start and end tag together exactly like HTML does
                // E.g. {#if condition...}
                val foldingRangeStartOffset = block.startTag.textRange.endOffset - 1
                val foldingRangeEndOffset = endTag.textRange.endOffset - 1
                val range = TextRange(foldingRangeStartOffset, foldingRangeEndOffset)

                descriptors.add(FoldingDescriptor(block, range))
            }
        }

        var child: PsiElement? = block.firstChild
        while (child != null) {
            appendDescriptors(child, descriptors, document)
            child = child.nextSibling
        }
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }

    private fun isSingleLine(element: PsiElement, document: Document): Boolean {
        val range = element.textRange
        return document.getLineNumber(range.startOffset) == document.getLineNumber(range.endOffset)
    }
}

package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
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

package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.psi.*
import java.util.*

// TODO class SvelteFoldingBuilder : FoldingBuilder, DumbAware {
class SvelteFoldingBuilder : FoldingBuilder {
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
            val open = PsiTreeUtil.findChildOfType(block, SvelteOpeningTag::class.java)

            val close = when (open) {
                is SvelteIfBlockOpeningTag -> PsiTreeUtil.findChildOfType(block, SvelteIfBlockClosingTag::class.java)
                is SvelteEachBlockOpeningTag -> PsiTreeUtil.findChildOfType(block, SvelteEachBlockClosingTag::class.java)
                is SvelteAwaitBlockOpeningTag -> PsiTreeUtil.findChildOfType(block, SvelteAwaitBlockClosingTag::class.java)
                is SvelteAwaitThenBlockOpeningTag -> PsiTreeUtil.findChildOfType(block, SvelteAwaitBlockClosingTag::class.java)
                else -> null
            }

            if (open != null && close != null) {
                // Following offsets ensure that we fold opening and closing tag together exactly like HTML does
                // E.g. {#if condition ...}
                val foldingRangeStartOffset = open.textRange.endOffset - 1
                val foldingRangeEndOffset = close.textRange.endOffset - 1
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

// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.format

import com.intellij.formatting.Alignment
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.formatting.templateLanguages.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.psi.SvelteTypes.HTML_FRAGMENT

val blocks = TokenSet.create()

/**
 * I don't really understand how it works.
 *
 * Based on Handlebars plugin
 */
class SvelteFormattingModelBuilder : TemplateLanguageFormattingModelBuilder() {
    override fun createTemplateLanguageBlock(node: ASTNode,
                                             wrap: Wrap?,
                                             alignment: Alignment?,
                                             foreignChildren: List<DataLanguageBlockWrapper>?,
                                             codeStyleSettings: CodeStyleSettings): TemplateLanguageBlock {
        val documentModel = FormattingDocumentModelImpl.createOn(node.psi.containingFile)
        val policy = HtmlPolicy(codeStyleSettings, documentModel)

        return SvelteBlock(node, wrap, alignment, this, codeStyleSettings, foreignChildren, policy)
    }
}

private class SvelteBlock(node: ASTNode,
                          wrap: Wrap?,
                          alignment: Alignment?,
                          blockFactory: TemplateLanguageBlockFactory,
                          settings: CodeStyleSettings,
                          foreignChildren: List<DataLanguageBlockWrapper>?,
                          private val myHtmlPolicy: HtmlPolicy) : TemplateLanguageBlock(node, wrap, alignment, blockFactory, settings, foreignChildren) {
    /**
     * We intend to indent the code in the following manner:
     * ```
     *      {#if condition}
     *          INDENTED_CONTENT
     *      {:else}
     *          INDENTED_CONTENT
     *      {/foo}
     * ```
     *
     * To understand the approach in this method, consider the following:
     * ```
     * {#if condition}
     * SVELTE_BLOCKS
     * MARKUP_FRAGMENT
     * SVELTE_BLOCKS
     * {/if}
     * ```
     *
     * Formatting seems easy. Simply apply an indent (represented here by `----`) to the SCOPE and call it a day:
     * ```
     * {#if condition}
     * ----SVELTE_BLOCKS
     * ----MARKUP_FRAGMENT
     * ----SVELTE_BLOCKS
     * {/if}
     * ```
     *
     * However, if we're contained in markup block, it's going to provide some indents of its own
     * (call them `::::`) which quickly leads to undesirable double-indenting:
     *
     * ```
     * <div>
     * ::::{#if condition}
     *     ----SVELTE_BLOCKS
     *     ::::----MARKUP_FRAGMENT
     *     ----SVELTE_BLOCKS
     * ::::{/if}
     * </div>
     * ```
     * So to behave correctly in both situations, we indent SCOPE from the "outside" anytime we're not wrapped
     * in a markup block, and we indent SCOPE from the "inside" (i.e. apply an indent to each non-markup block
     * inside the SCOPE) to interleave nicely with markup language provided indents.
     */
    override fun getIndent(): Indent? {
        // ignore whitespace-only blocks
        // TODO Check if this branch is necessary
        if (myNode.text.trim().isEmpty()) {
            return Indent.getNoneIndent()
        }



        // any element that is the direct descendant of a foreign block gets an indent
        // (unless that foreign element has been configured to not indent its children)
        val foreignParent = getForeignBlockParent(true)
        return if (foreignParent != null) {
            if (foreignParent.node is XmlTag && !myHtmlPolicy.indentChildrenOf(foreignParent.node as XmlTag?)) {
                Indent.getNoneIndent()
            } else {
                Indent.getNormalIndent()
            }
        } else {
            Indent.getNoneIndent()
        }
    }

    /**
     * This method handles indent on Enter
     */
    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (parent is DataLanguageBlockWrapper) {
            println(myNode.elementType)
        }
//        return if (openings.contains(myNode.elementType)
//                || (parent is DataLanguageBlockWrapper
//                        && (myNode.elementType !== SvelteTypes.SCOPE || myNode.treeNext is PsiErrorElement))) {
        return if (blocks.contains(myNode.elementType)) {
            ChildAttributes(Indent.getNormalIndent(), null)
        } else {
            ChildAttributes(Indent.getNoneIndent(), null)
        }
    }

    override fun getTemplateTextElementType(): IElementType = HTML_FRAGMENT

    override fun isRequiredRange(range: TextRange?): Boolean = false

    /**
     * Returns this block's first "real" foreign block parent if it exists, and null otherwise.
     * ("real" means not SyntheticBlock inserted by the template formatter)
     *
     * @param immediate Pass true to only check for an immediate foreign parent, false to look up the hierarchy.
     */
    private fun getForeignBlockParent(immediate: Boolean): DataLanguageBlockWrapper? {
        var foreignBlockParent: DataLanguageBlockWrapper? = null
        var parent: BlockWithParent? = parent

        while (parent != null) {
            if (parent is DataLanguageBlockWrapper && parent.original !is SyntheticBlock) {
                foreignBlockParent = parent
                break
            } else if (immediate && parent is SvelteBlock) {
                break
            }
            parent = parent.parent
        }

        return foreignBlockParent
    }
}

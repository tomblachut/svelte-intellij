package dev.blachut.svelte.lang.format

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageFormatting
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.formatter.xml.AbstractXmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteExpressionBlock(
  node: ASTNode,
  private val indent: Indent?,
  wrap: Wrap?,
  private val policy: XmlFormattingPolicy
) :
  AbstractBlock(node, wrap, null) {
  override fun isLeaf(): Boolean = false

  override fun buildChildren(): MutableList<Block> {
    val results = ArrayList<Block>(4)

    // borrowed from com.intellij.psi.formatter.common.InjectedLanguageBlockBuilder.addInjectedLanguageBlockWrapper
    val nodePsi = myNode.psi
    val builder = LanguageFormatting.INSTANCE.forContext(nodePsi.language, nodePsi)

    var child = myNode.firstChildNode
    while (child != null) {
      if (child.textLength > 0 && !AbstractXmlBlock.containsWhiteSpacesOnly(child)) {
        if (child.elementType === JSTokenTypes.LBRACE) {
          val startTag = SvelteTagElementTypes.START_TAGS.contains(myNode.elementType)
          val wrap = if (startTag) Wrap.createWrap(WrapType.ALWAYS, true) else null
          results.add(SvelteLeafBlock(child, wrap = wrap))
        }
        else if (child.elementType === JSTokenTypes.RBRACE) {
          results.add(SvelteLeafBlock(child, indent = Indent.getNoneIndent()))
        }
        else {
          if (builder != null) {
            val childModel = builder.createModel(FormattingContext.create(child.psi, policy.settings))
            results.add(childModel.rootBlock)
          }
          else {
            results.add(SvelteLeafBlock(child))
          }
        }
      }

      child = child.treeNext
    }

    return results
  }

  override fun getSpacing(child1: Block?, child2: Block): Spacing? {
    if (child1 !is ASTBlock || child2 !is ASTBlock) {
      return null
    }

    val node1 = child1.node ?: return null
    val node2 = child2.node ?: return null

    val type1 = node1.elementType
    val type2 = node2.elementType

    if (SvelteTokenTypes.KEYWORDS.contains(type1) && type2 !== JSTokenTypes.RBRACE) {
      return Spacing.createSpacing(1, 1, 0, true, 0)
    }

    return null
  }

  override fun getIndent(): Indent? {
    return indent
  }
}

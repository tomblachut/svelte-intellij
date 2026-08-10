package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType

/*
 * Lookups over the header of a start tag, i.e. the attribute area between the tag name and the `>` or `/>`.
 * Both return the start tag's tokens even when the tag has an end tag, because the end tag's tokens come
 * later in the tag node.
 */

private val TAG_HEADER_END = TokenSet.create(XmlTokenType.XML_TAG_END, XmlTokenType.XML_EMPTY_ELEMENT_END)

/** The offset right after the tag name, where the start tag header begins, or null for a nameless tag. */
internal fun startTagHeaderStart(tagNode: ASTNode): Int? =
  tagNode.findChildByType(XmlTokenType.XML_NAME)?.textRange?.endOffset

/** The `>` or `/>` token ending the start tag header, or null when the tag is unclosed. */
internal fun startTagEndToken(tagNode: ASTNode): ASTNode? = tagNode.findChildByType(TAG_HEADER_END)

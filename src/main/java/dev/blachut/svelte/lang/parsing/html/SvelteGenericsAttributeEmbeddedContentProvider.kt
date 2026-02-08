// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlAttributeEmbeddedContentProvider
import com.intellij.html.embedding.HtmlEmbedmentInfo
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.psi.SvelteGenericsEmbeddedContentTokenType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

/**
 * Embedded content provider for the `generics` attribute on script tags.
 *
 * This enables TypeScript parsing of the attribute value, allowing proper
 * type parameter support for generic Svelte components.
 *
 * Example: `<script lang="ts" generics="T extends { text: string }">`
 */
class SvelteGenericsAttributeEmbeddedContentProvider(lexer: BaseHtmlLexer) : HtmlAttributeEmbeddedContentProvider(lexer) {

  override fun isInterestedInTag(tagName: CharSequence): Boolean =
    namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME)

  override fun isInterestedInAttribute(attributeName: CharSequence): Boolean =
    namesEqual(attributeName, GENERICS_ATTRIBUTE_NAME)

  override fun createEmbedmentInfo(): HtmlEmbedmentInfo = SvelteGenericsEmbedmentInfo

  /**
   * Override to include Svelte mustache tokens as valid embedment tokens.
   * This allows curly braces in generics constraints like `T extends { text: string }`
   * to be treated as part of the attribute value rather than Svelte expressions.
   */
  override fun isAttributeEmbedmentToken(tokenType: IElementType): Boolean =
    super.isAttributeEmbedmentToken(tokenType) ||
    tokenType === SvelteTokenTypes.START_MUSTACHE ||
    tokenType === SvelteTokenTypes.END_MUSTACHE ||
    tokenType === SvelteTokenTypes.CODE_FRAGMENT

  companion object {
    const val GENERICS_ATTRIBUTE_NAME: String = "generics"
  }
}

/**
 * Embedment info for the generics attribute value.
 * Provides the element type for parsing and lexer for syntax highlighting.
 */
private object SvelteGenericsEmbedmentInfo : HtmlEmbedmentInfo {
  override fun getElementType(): IElementType =
    SvelteGenericsEmbeddedContentTokenType.INSTANCE

  override fun createHighlightingLexer(): Lexer =
    JavaScriptHighlightingLexer(TypeScriptLanguageDialect.optionHolder)
}

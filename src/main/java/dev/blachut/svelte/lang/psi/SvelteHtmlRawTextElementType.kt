// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.psi

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlRawTextImpl
import com.intellij.psi.tree.IStrongWhitespaceHolderElementType
import com.intellij.psi.xml.XmlElementType.XML_TEXT
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlParsing
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlRawTextLexer

object SvelteHtmlRawTextElementType : HtmlCustomEmbeddedContentTokenType("SVELTE_HTML_RAW_TEXT", SvelteHTMLLanguage.INSTANCE),
                                      IStrongWhitespaceHolderElementType {

  override fun parse(builder: PsiBuilder) {
    val svelteParsing = SvelteHtmlParsing(builder)
    val start = builder.mark()
    var text: Marker? = null
    while (!builder.eof()) {
      if (builder.tokenType == SvelteTokenTypes.START_MUSTACHE) {
        text?.done(XML_TEXT)
        text = null
        svelteParsing.parseSvelteTag()
      }
      else {
        if (svelteParsing.hasOpenedBlocks() && text == null) {
          text = builder.mark()
        }
        builder.advanceLexer()
      }
    }
    text?.done(XML_TEXT)
    start.done(XML_TEXT)
  }

  override fun createLexer(): Lexer = SvelteHtmlRawTextLexer()

  override fun createPsi(node: ASTNode): PsiElement = HtmlRawTextImpl(node)
}
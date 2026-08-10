package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.html.HtmlParsing
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHTMLParserDefinition : HTMLParserDefinition() {
  override fun createLexer(project: Project?): Lexer {
    return SvelteHtmlLexer(false)
  }

  override fun createParser(project: Project?): PsiParser {
    return object : HTMLParser() {
      override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
        return SvelteHtmlParsing(builder)
      }
    }
  }

  override fun getFileNodeType(): IFileElementType {
    return SvelteHtmlFileElementType.FILE
  }

  /**
   * [HTMLParser] disables the [PsiBuilder]'s automatic comment skipping, so this does not affect parsing.
   * It makes the tag header comments materialize as [com.intellij.psi.PsiComment] leaves and enables the
   * platform features keyed off comment tokens, such as TODO indexing.
   */
  override fun getCommentTokens(): TokenSet {
    return COMMENTS
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return SvelteHtmlFile(viewProvider)
  }

  override fun createElement(node: ASTNode): PsiElement {
    return try {
      SvelteElementTypes.createElement(node)
    }
    catch (e: Exception) {
      super.createElement(node)
    }
  }
}

/**
 * Top level rather than a field of the parser definition: every [com.intellij.lang.ParserDefinition] is created
 * at startup, and a field would make that create and register every token type held by the referenced classes,
 * even for projects without a single Svelte file. Here they are touched only once
 * [SvelteHTMLParserDefinition.getCommentTokens] is called.
 */
private val COMMENTS = TokenSet.orSet(XmlTokenType.COMMENTS, SvelteTokenTypes.TAG_COMMENTS)

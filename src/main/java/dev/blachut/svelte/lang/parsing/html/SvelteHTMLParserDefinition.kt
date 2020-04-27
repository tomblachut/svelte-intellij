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
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteHTMLParserDefinition : HTMLParserDefinition() {
    override fun createLexer(project: Project): Lexer {
        return SvelteHtmlLexer()
    }

    override fun createParser(project: Project?): PsiParser {
        return object : HTMLParser() {
            override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
                return SvelteHtmlParsing(builder)
            }
        }
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return SvelteHtmlFile(viewProvider)
    }

    override fun createElement(node: ASTNode): PsiElement {
        return try {
            SvelteElementTypes.createElement(node)
        } catch (e: Exception) {
            super.createElement(node)
        }
    }

    companion object {
        val FILE = IFileElementType(SvelteHTMLLanguage.INSTANCE)
    }
}

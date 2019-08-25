package dev.blachut.svelte.lang

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.html.HtmlParsing
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType

class SvelteHTMLParserDefinition : HTMLParserDefinition() {
    override fun createLexer(project: Project): Lexer {
        return SvelteHtmlLexer()
    }

    override fun createParser(project: Project?): PsiParser {
        return object : HTMLParser() {
            override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
                return object : HtmlParsing(builder) {
                    override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
                        // Inspired by Vue plugin. Svelte tags must be closed explicitly
                        if (isSvelteComponentTag(originalTagName)) {
                            return false
                        }
                        return super.isSingleTag(tagName, originalTagName)
                    }
                }
            }
        }
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return HtmlFileImpl(viewProvider, FILE)
    }

    companion object {
        val FILE = IFileElementType(SvelteHTMLLanguage.INSTANCE)
    }
}

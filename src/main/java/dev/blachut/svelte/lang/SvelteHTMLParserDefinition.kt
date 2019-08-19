package dev.blachut.svelte.lang

import com.intellij.lang.html.HTMLParserDefinition
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

package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

class SvelteJSScriptContentProvider : HtmlScriptContentProvider {
    override fun getScriptElementType(): IElementType = SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE

    override fun getHighlightingLexer(): Lexer? {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteJSLanguage.INSTANCE, null, null).highlightingLexer
    }

    companion object {
        fun getJsEmbeddedContent(script: PsiElement?): JSEmbeddedContent? {
            return PsiTreeUtil.getChildOfType(script, JSEmbeddedContent::class.java)
        }
    }
}

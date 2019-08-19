package dev.blachut.svelte.lang

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.tree.IElementType

val SVELTE_JS_EMBEDDED_CONTENT_MODULE: IElementType = JSEmbeddedContentElementType(SvelteJSLanguage.INSTANCE, "MOD_SVELTE_JS_")

class SvelteHtmlLexer : HtmlLexer() {
    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
        return object : HtmlScriptContentProvider {
            override fun getScriptElementType(): IElementType = JSStubElementTypes.ES6_EMBEDDED_CONTENT_MODULE

            override fun getHighlightingLexer(): Lexer? {
//                return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteJSLanguage.INSTANCE, null, null).highlightingLexer
                return SyntaxHighlighterFactory.getSyntaxHighlighter(JSLanguageLevel.ES6.dialect, null, null).highlightingLexer
            }
        }
    }
}

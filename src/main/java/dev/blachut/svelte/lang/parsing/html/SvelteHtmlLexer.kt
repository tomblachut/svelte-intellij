package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lexer.*
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage

class SvelteHtmlLexer : HtmlLexer(BaseSvelteHtmlLexer(), false) {
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        val baseState = initialState and 0xffff
        val nestingLevel = initialState shr 16
        (delegate as BaseSvelteHtmlLexer).flexLexer.bracesNestingLevel = nestingLevel
        super.start(buffer, startOffset, endOffset, baseState)
    }

    override fun getState(): Int {
        val nestingLevel = (delegate as BaseSvelteHtmlLexer).flexLexer.bracesNestingLevel
        return (nestingLevel shl 16) or (super.getState() and 0xffff)
    }

    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
        return object : HtmlScriptContentProvider {
            override fun getScriptElementType(): IElementType = JSStubElementTypes.ES6_EMBEDDED_CONTENT_MODULE

            override fun getHighlightingLexer(): Lexer? {
//                return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteJSLanguage.INSTANCE, null, null).highlightingLexer
                return SyntaxHighlighterFactory.getSyntaxHighlighter(JSLanguageLevel.ES6.dialect, null, null).highlightingLexer
            }
        }
    }

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }
}

val SVELTE_JS_EMBEDDED_CONTENT_MODULE: IElementType = JSEmbeddedContentElementType(SvelteJSLanguage.INSTANCE, "MOD_SVELTE_JS_")

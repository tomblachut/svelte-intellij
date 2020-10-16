package dev.blachut.svelte.lang.parsing.ts

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

class SvelteTypeScriptScriptContentProvider : HtmlScriptContentProvider {
    override fun getScriptElementType(): IElementType = SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE_TS

    override fun getHighlightingLexer(): Lexer {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(
            SvelteTypeScriptLanguage.INSTANCE,
            null,
            null
        ).highlightingLexer
    }
}

package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.psi.tree.IElementType

// Adapted from org.jetbrains.vuejs.lang.html.lexer.VueLexerHandle
interface SvelteHtmlLexerHandle {
    fun registerHandler(elementType: IElementType, value: BaseHtmlLexer.TokenHandler)

    var seenTag: Boolean
    val seenScript: Boolean
    var seenStyleType: Boolean
    var seenContentType: Boolean
    val seenStyle: Boolean
    val styleType: String?
    val inTagState: Boolean
}

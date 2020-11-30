package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.validation.TypeScriptKeywordHighlighterVisitor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTokenType
import com.intellij.refactoring.suggested.startOffset
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.parsing.html.SvelteDirectiveLexer
import dev.blachut.svelte.lang.psi.*

class SvelteKeywordHighlighterVisitor(holder: HighlightInfoHolder) : TypeScriptKeywordHighlighterVisitor(holder),
    SvelteVisitor {
    override fun visitInitialTag(tag: SvelteInitialTag) {
        highlightChildKeywordOfType(tag, SvelteTokenTypes.AS_KEYWORD)
        highlightChildKeywordOfType(tag, SvelteTokenTypes.THEN_KEYWORD)
        super.visitInitialTag(tag)
    }

    override fun visitLazyElement(element: SvelteJSLazyPsiElement) {
        highlightChildKeywordOfType(element, SvelteTokenTypes.HTML_KEYWORD)
        highlightChildKeywordOfType(element, SvelteTokenTypes.DEBUG_KEYWORD)

        super.visitLazyElement(element)
    }

    override fun visitJSLabeledStatement(node: JSLabeledStatement) {
        if (node.label == SvelteReactiveDeclarationsUtil.REACTIVE_LABEL) {
            highlightChildKeywordOfType(node, JSTokenTypes.IDENTIFIER)
        }

        super.visitJSLabeledStatement(node)
    }

    override fun visitElement(element: PsiElement) {
        if (element is SvelteHtmlAttribute && element.directive != null) {
            val startOffset = element.nameElement.startOffset
            highlight(JSHighlighter.JS_KEYWORD, TextRange(startOffset, element.textOffset))

            val lexer = SvelteDirectiveLexer()
            lexer.start(element.name)
            while (lexer.tokenType != null) {
                if (lexer.tokenType == JSTokenTypes.OR) {
                    highlight(JSHighlighter.ES6_DECORATOR,
                        TextRange(lexer.tokenStart, lexer.tokenEnd).shiftRight(startOffset))
                } else if (lexer.tokenType == XmlTokenType.XML_NAME) {
                    highlight(JSHighlighter.ES6_DECORATOR,
                        TextRange(lexer.tokenStart, lexer.tokenEnd).shiftRight(startOffset))
                }

                lexer.advance()
            }
        }

        super.visitElement(element)
    }

    private fun highlight(key: TextAttributesKey, range: TextRange) {
        val mappedKey = myHighlighter.getMappedKey(key)
        val info = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION)
            .severity(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
            .range(range).textAttributes(mappedKey).create()

        myHolder.add(info)
    }
}

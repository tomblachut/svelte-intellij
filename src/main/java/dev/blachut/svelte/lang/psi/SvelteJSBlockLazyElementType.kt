package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.parsing.html.SvelteJSExpressionLexer

// TODO Merge SvelteJSBlockLazyElementType & SvelteJSLazyElementType
abstract class SvelteJSBlockLazyElementType(debugName: String) :
    ILazyParseableElementType(debugName, SvelteJSLanguage.INSTANCE) {
    protected abstract val noTokensErrorMessage: String
    protected open val excessTokensErrorMessage = "Unexpected token"

    protected open val assumeExternalBraces: Boolean = false

    override fun createNode(text: CharSequence?): ASTNode? {
        text ?: return null
        return SvelteInitialTag(this, text)
    }

    override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
        val project = psi.project
        val lexer = SvelteJSExpressionLexer(assumeExternalBraces)
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.chars)
        val parser = JSLanguageUtil.createJSParser(language, builder)

        val rootMarker = builder.mark()

        if (builder.eof()) {
            builder.error(noTokensErrorMessage)
        } else {
            if (!assumeExternalBraces) {
                builder.remapCurrentToken(JSTokenTypes.LBRACE)
                builder.advanceLexer()
            }
            parseTokens(builder, parser)
            if (!assumeExternalBraces) {
                builder.remapCurrentToken(JSTokenTypes.RBRACE)
                builder.advanceLexer()
            }

            ensureEof(builder)
        }

        rootMarker.done(this)

        return builder.treeBuilt.firstChildNode
    }

    protected abstract fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>)

    private fun ensureEof(builder: PsiBuilder) {
        if (!builder.eof()) {
            builder.error(excessTokensErrorMessage)
            while (!builder.eof()) {
                builder.advanceLexer()
            }
        }
    }
}

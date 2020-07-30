package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.SvelteJSLanguage

abstract class SvelteJSLazyElementType(debugName: String) : ILazyParseableElementType(debugName, SvelteJSLanguage.INSTANCE) {
    protected abstract val noTokensErrorMessage: String
    protected val excessTokensErrorMessage = "unexpected token"
    
    fun createJavaScriptParser(builder: PsiBuilder) = ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>,
        ES6FunctionParser<*>, JSPsiTypeParser<*>>(builder)

    override fun createNode(text: CharSequence?): ASTNode? {
        text ?: return null
        return SvelteJSLazyPsiElement(this, text)
    }

    override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
        val project = psi.project
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, SvelteJSLanguage.INSTANCE, chameleon.chars)
        val parser = createJavaScriptParser(builder)

        val rootMarker = builder.mark()

        if (builder.eof()) {
            builder.error(noTokensErrorMessage)
        } else {
            parseTokens(builder, parser)
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

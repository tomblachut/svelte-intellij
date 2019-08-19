package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.parsing.JavaScriptParser

object SvelteJSLazyElementTypes {
    val PARAMETER = object : SvelteJSLazyElementType("PARAMETER") {
        override fun parseJS(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
            ensureEof(builder, "( or } expected")
        }
    }

    val EXPRESSION = object : SvelteJSLazyElementType("EXPRESSION") {
        override fun parseJS(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parser.expressionParser.parseExpression()
            ensureEof(builder, "expression expected")
        }
    }
}

package dev.blachut.svelte.lang

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import dev.blachut.svelte.lang.psi.*

class SvelteCodeMultiHostInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(SvelteFile::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
//        println("getLanguagesToInject")
        val visitor = SvelteCodeInjectionVisitor(registrar)
        visitor.visitElement(context.containingFile)
        if (visitor.started) {
            registrar.doneInjecting()
//             For debug
//             (registrar as InjectionRegistrarImpl).resultFiles[0].viewProvider.virtualFile
//             println(registrar.toString())
        }
    }
}

class SvelteCodeInjectionVisitor(private val registrar: MultiHostRegistrar) : SvelteVisitor() {
    var started = false

    private val expressionPrefix = "String("
    private val expressionSuffix = ");"

    private var danglingPrefix = "'use strict';"


    override fun visitIfBlock(ifBlock: SvelteIfBlock) {
        val expression = ifBlock.ifBlockOpening.ifBlockOpeningTag.expression

        if (expression != null) {
            stitchScript("if (", expression, ") {")
        } else {
            appendPrefix("if (undefined) {")
        }

        visitScope(ifBlock.ifBlockOpening.scope)

        for (elseIfContinuation in ifBlock.elseIfContinuationList) {
            val innerExpression = elseIfContinuation.elseIfContinuationTag.expression
            if (innerExpression != null) {
                stitchScript("} else if (", innerExpression, ") {")
            } else {
                appendPrefix("} else if (undefined) {")
            }
            visitScope(elseIfContinuation.scope)
        }

        val elseContinuation = ifBlock.elseContinuation
        if (elseContinuation != null) {
            appendPrefix("} else {")
            visitScope(elseContinuation.scope)
        }

        appendPrefix("}")
    }

    /**
     * This method stitches together call to forEach Array method
     *
     * IN:
     * {#each items as item, index (key)}
     * Note that [as item] part is currently required
     *
     * items: SvelteExpression
     * item: SvelteParameter
     * index: SvelteParameter?
     * key: SvelteExpression?
     *
     * OUT:
     *  if (items) {
     *      [...items].forEach((item, index) => {
     *          String(key);
     *          <each body>
     *      });
     *  } else {
     *      <else body>
     *  }
     */
    override fun visitEachBlock(eachBlock: SvelteEachBlock) {
        val openingTag = eachBlock.eachBlockOpening.eachBlockOpeningTag
        val items = openingTag.expressionList.getOrNull(0)
        val item = openingTag.parameterList.getOrNull(0)
        val index = openingTag.parameterList.getOrNull(1)
        val key = openingTag.expressionList.getOrNull(1)

        if (items != null) {
            stitchScript("if (${items.text}) { const __iterable = ", items)
        } else {
            appendPrefix("if (undefined) { const __iterable = []") // in case of parse error
        }
        appendPrefix("; for (const __i = 0; __i < __iterable.length; __i++) {")

        if (item != null) stitchScript("const ", item, " = __iterable[__i];")
        if (index != null) stitchScript("const ", index, " = __i;")
        if (key != null) stitchScript(expressionPrefix, key, expressionSuffix)

        visitScope(eachBlock.eachBlockOpening.scope)

        appendPrefix("}") // for end

        val elseContinuation = eachBlock.elseContinuation
        if (elseContinuation != null) {
            appendPrefix("} else {") // end if, start else
            visitScope(elseContinuation.scope)
        }

        appendPrefix("}") // if/else end
    }

    override fun visitAwaitBlock(awaitBlock: SvelteAwaitBlock) {
        val promiseExpression: SvelteExpression?
        val thenParameter: SvelteParameter?
        val awaitScope: SvelteScope?
        val thenScope: SvelteScope?

        val awaitThenBlockOpening = awaitBlock.awaitThenBlockOpening
        if (awaitThenBlockOpening != null) {
            promiseExpression = awaitThenBlockOpening.awaitThenBlockOpeningTag.expression
            thenParameter = awaitThenBlockOpening.awaitThenBlockOpeningTag.parameter
            awaitScope = null
            thenScope = awaitThenBlockOpening.scope
        } else {
            val awaitBlockOpening = awaitBlock.awaitBlockOpening!!

            promiseExpression = awaitBlockOpening.awaitBlockOpeningTag.expression
            thenParameter = awaitBlock.thenContinuation?.thenContinuationTag?.parameter
            awaitScope = awaitBlockOpening.scope
            thenScope = awaitBlock.thenContinuation?.scope
        }

        if (awaitScope != null) visitScope(awaitScope)

        if (promiseExpression != null) {
            stitchScript("new Promise(", promiseExpression, ")")
        } else {
            appendPrefix("Promise.resolve()")
        }

        if (thenParameter != null) {
            stitchScript(".then((", thenParameter, ") => {")
        } else {
            appendPrefix(".then(() => {")
        }

        if (thenScope != null) visitScope(thenScope)

        val catchContinuation = awaitBlock.catchContinuation
        if (catchContinuation != null) {
            val catchParameter = catchContinuation.catchContinuationTag.parameter
            if (catchParameter != null) {
                stitchScript("}).catch((", catchParameter, ") => {")
            } else {
                appendPrefix("}).catch(() => {")
            }

            visitScope(catchContinuation.scope)
        }

        appendPrefix("});")
    }

    override fun visitInterpolation(context: SvelteInterpolation) {
        val expression = context.expression
        if (expression != null) stitchScript(expressionPrefix, expression, expressionSuffix)
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }

    private fun appendPrefix(prefix: String) {
        danglingPrefix += prefix
    }

    /**
     * Adds script injection place. Handles dangling prefix, so that resulting file is well formed
     * After visitor completes traversal there can be remaining prefix, but IntelliJ doesn't mind
     */
    private fun stitchScript(prefix: String, host: PsiLanguageInjectionHost, suffix: String? = null) {
        if (!started) {
            registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_6, "js")
            started = true
        }
        registrar.addPlace(prependDanglingPrefix(prefix), suffix, host, TextRange.from(0, host.textLength))
    }

    /**
     * Consumes accumulated danglingPrefix and prepends provided prefix with it
     */
    private fun prependDanglingPrefix(basePrefix: String): String {
        return if (danglingPrefix.isNotEmpty()) {
            val newPrefix = danglingPrefix + basePrefix
            danglingPrefix = ""
            newPrefix
        } else {
            basePrefix
        }
    }
}
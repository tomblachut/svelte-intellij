package dev.blachut.svelte.lang

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.*

class SvelteCodeMultiHostInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        println("elementsToInjectIn")
        return mutableListOf(SvelteFile::class.java)
//        return mutableListOf(SvelteExpressionImpl::class.java, SvelteParameterImpl::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        println("getLanguagesToInject")
        val visitor = SvelteCodeInjectionVisitor(registrar)
        visitor.visitElement(context)
        if (visitor.started) {
            registrar.doneInjecting()
        }
    }
}


class SvelteCodeInjectionVisitor(private val registrar: MultiHostRegistrar) : SvelteVisitor() {
    var started = false

    private val standardExpressionPrefix = "String("
    private val standardExpressionSuffix = ");"

    private var danglingPrefix = "'use strict';"

    override fun visitEachBlock(eachBlockElement: SvelteEachBlock) {
        /*
        This method stitches together call to forEach Array method

        IN:
        {#each items as item, index (key)}

        items: SvelteExpression
        item: SvelteParameter
        index: SvelteParameter
        key: SvelteExpression

        Note that as item part is currently required

        OUT:

        if (items) {
            [...items].forEach((item, index) => {
                String(key);
                <each body>
            });
        } else {
            <else body>
        }
        */

        if (!started) {
            registrar.startInjecting(JavascriptLanguage.INSTANCE)
            started = true
        }

        val items = eachBlockElement.eachBlockOpening.expressionList[0]
        val item = eachBlockElement.eachBlockOpening.parameterList[0]
        val index = eachBlockElement.eachBlockOpening.parameterList.getOrNull(1)
        val key = eachBlockElement.eachBlockOpening.expressionList.getOrNull(1)

        var ifPrefix = "if (${items.text}) { [..."
        if (danglingPrefix.isNotEmpty()) {
            ifPrefix = danglingPrefix + standardExpressionPrefix
            danglingPrefix = ""
        }

        registrar.addPlace(ifPrefix, "]", items, range(items))
        registrar.addPlace(".forEach((", "", item, range(item))

        val arrowDelimiter = ")=>{"
        var arrowDelimiterAppended = false

        if (index != null) {
            registrar.addPlace(",", arrowDelimiter, index, range(index))
            arrowDelimiterAppended = true
        }

        if (key != null) {
            val prefix = if (arrowDelimiterAppended) standardExpressionPrefix else arrowDelimiter + standardExpressionPrefix
            arrowDelimiterAppended = true
            registrar.addPlace(prefix, standardExpressionSuffix, key, range(key))
        }


        if (!arrowDelimiterAppended) {
            danglingPrefix += arrowDelimiter
        }


        val eachBodyElement = eachBlockElement.scopeList[0] // Guaranteed by the parser
        visitScope(eachBodyElement)

        danglingPrefix += "})}"

        if (eachBlockElement.elseContinuation != null) {
            danglingPrefix += "else {"
            visitScope(eachBlockElement.scopeList[1])
            danglingPrefix += "}"
        }
    }

    override fun visitExpression(context: SvelteExpression) {
        println(context.textRange)
        if (!started) {
            registrar.startInjecting(JavascriptLanguage.INSTANCE)
            started = true
        }

        var prefix = standardExpressionPrefix
        if (danglingPrefix.isNotEmpty()) {
            prefix = danglingPrefix + standardExpressionPrefix
            danglingPrefix = ""
        }
        registrar.addPlace(prefix, standardExpressionSuffix, context, range(context))
    }

    override fun visitParameter(context: SvelteParameter) {
        println(context.textRange)
        if (!started) {
            registrar.startInjecting(JavascriptLanguage.INSTANCE)
            started = true
        }

        var prefix = "("
        if (danglingPrefix.isNotEmpty()) {
            prefix = danglingPrefix + standardExpressionPrefix
            danglingPrefix = ""
        }
        registrar.addPlace(prefix, ") => null;", context, range(context))
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }
}

fun range(length: PsiElement): TextRange {
    return TextRange.from(0, length.textLength)
}
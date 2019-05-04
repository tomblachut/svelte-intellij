package dev.blachut.svelte.lang

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JavascriptLanguage
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
    override fun visitEachBlock(eachBlockElement: SvelteEachBlock) {
        ensureStartInjecting()

        val items = eachBlockElement.eachBlockOpening.expressionList.getOrNull(0)
        val item = eachBlockElement.eachBlockOpening.parameterList.getOrNull(0)
        val index = eachBlockElement.eachBlockOpening.parameterList.getOrNull(1)
        val key = eachBlockElement.eachBlockOpening.expressionList.getOrNull(1)

        if (items != null) {
            stitchScript("if (${items.text}) { [...", items, "]") // nullish guard, cast Iterable to Array
        } else {
            appendPrefix("if (false) { []") // in case of parse error

        }
        if (item != null) {
            stitchScript(".forEach((", item)
        } else {
            appendPrefix(".forEach((")
        }

        if (index != null) stitchScript(",", index)

        danglingPrefix += ") => {"

        if (key != null) stitchScript(expressionPrefix, key, expressionSuffix)

        // Scope after each block opening  is guaranteed by the parser
        visitScope(eachBlockElement.scopeList[0])

        danglingPrefix += "}) }" // arrow end, forEach end, if end

        if (eachBlockElement.elseContinuation != null) {
            danglingPrefix += "else {"
            visitScope(eachBlockElement.scopeList[1])
            danglingPrefix += "}" // else end
        }
    }

    override fun visitExpression(context: SvelteExpression) {
        ensureStartInjecting()
        stitchScript(expressionPrefix, context, expressionSuffix)
    }

    override fun visitParameter(context: SvelteParameter) {
        ensureStartInjecting()
        stitchScript("(", context, ") => null;")
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }

    private fun ensureStartInjecting() {
        if (!started) {
            registrar.startInjecting(JavascriptLanguage.INSTANCE, "js")
            started = true
        }
    }

    private fun appendPrefix(prefix: String) {
        danglingPrefix += prefix
    }

    /**
     * Adds script injection place. Handles dangling prefix, so that resulting file is well formed
     * After visitor completes traversal there can be remaining prefix, but IntelliJ doesn't mind
     */
    private fun stitchScript(prefix: String, host: PsiLanguageInjectionHost, suffix: String? = null) {
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
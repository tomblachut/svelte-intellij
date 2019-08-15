package dev.blachut.svelte.lang

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import dev.blachut.svelte.lang.psi.SvelteExpression
import dev.blachut.svelte.lang.psi.SvelteFile
import dev.blachut.svelte.lang.psi.SvelteParameter
import dev.blachut.svelte.lang.psi.SvelteVisitor

class SvelteCodeMultiHostInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(SvelteFile::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
       SvelteCodeInjectionVisitor(registrar).visitElement(context.containingFile)
    }
}

class SvelteCodeInjectionVisitor(private val registrar: MultiHostRegistrar) : SvelteVisitor() {
    override fun visitParameter(parameter: SvelteParameter) {
        inject("{\nconst [", parameter, "] = []\n}")
    }

    override fun visitExpression(expression: SvelteExpression) {
        // Taken from JSChangeUtil.tryCreateExpressionInternal
        inject("{\n(", expression, ")\n}")
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }

    private fun inject(prefix: String, host: PsiLanguageInjectionHost, suffix: String) {
        registrar.startInjecting(SvelteJSLanguage.INSTANCE)
        registrar.addPlace(prefix, suffix, host, TextRange.from(0, host.textLength))
        registrar.doneInjecting()
    }
}

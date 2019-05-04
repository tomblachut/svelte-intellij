package dev.blachut.svelte.lang

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import dev.blachut.svelte.lang.psi.impl.SvelteExpressionImpl
import dev.blachut.svelte.lang.psi.impl.SvelteParameterImpl

class SvelteCodeMultiHostInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        println("elementsToInjectIn")
//        return mutableListOf(SvelteFile::class.java)
//        return mutableListOf()
        return mutableListOf(SvelteExpressionImpl::class.java, SvelteParameterImpl::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        println("getLanguagesToInject")
        registrar.startInjecting(JavascriptLanguage.INSTANCE)
        registrar.addPlace("String(", ");", context as (PsiLanguageInjectionHost), TextRange.from(0, context.textLength))
        registrar.doneInjecting()
    }
}
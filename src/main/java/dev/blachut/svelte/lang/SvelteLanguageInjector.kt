package dev.blachut.svelte.lang

import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import dev.blachut.svelte.lang.psi.SvelteExpression

class SvelteLanguageInjector : LanguageInjector {
    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, registrar: InjectedLanguagePlaces) {
        if (host is SvelteExpression) {
            registrar.addPlace(JavaScriptSupportLoader.ECMA_SCRIPT_6, TextRange.from(0, host.textLength), null, null)
        }
    }

}

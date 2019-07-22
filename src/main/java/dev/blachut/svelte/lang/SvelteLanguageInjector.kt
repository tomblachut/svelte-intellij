package dev.blachut.svelte.lang

import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import dev.blachut.svelte.lang.psi.SvelteInterpolation

class SvelteLanguageInjector : LanguageInjector {
    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, registrar: InjectedLanguagePlaces) {
        if (host is XmlAttributeValue) {
            injectJsInAttributeValue(host, registrar)
        }
    }

    private fun injectJsInAttributeValue(host: PsiLanguageInjectionHost, registrar: InjectedLanguagePlaces) {
        val attribute = (host as XmlAttributeValue)
        val value = attribute.text
        if (value.contains("{") && value.contains("}")) {
            val file = PsiFileFactory.getInstance(attribute.project)
                .createFileFromText("dummy.svelte", SvelteLanguage.INSTANCE, value, true, true)
            val interpolations = PsiTreeUtil.findChildrenOfType(file, SvelteInterpolation::class.java)
            for (interpolation in interpolations) {
                registrar.addPlace(SvelteLanguage.INSTANCE, interpolation.textRange, null, null)
                registrar.addPlace(JavaScriptSupportLoader.ECMA_SCRIPT_6, interpolation.expression.textRange, null, null)
            }
        }
    }

}

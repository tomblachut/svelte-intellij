package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.isModuleScript

class SvelteKitImplicitUsageProvider : ImplicitUsageProvider {
    private val functionNames = setOf("load")
    private val variableNames = setOf("hydrate", "prerender", "router", "ssr")

    override fun isImplicitUsage(element: PsiElement): Boolean {
        if (element is JSElementBase && element.containingFile is SvelteHtmlFile && element.isExported) {
            if (element is JSVariable && variableNames.contains(element.name) ||
                element is JSFunction && functionNames.contains(element.name)) {
                return isModuleScript(element.parentOfType<HtmlTag>())
            }
        }

        // todo hooks

        return false
    }

    override fun isImplicitRead(element: PsiElement): Boolean {
        return false
    }

    override fun isImplicitWrite(element: PsiElement): Boolean {
        return false
    }
}

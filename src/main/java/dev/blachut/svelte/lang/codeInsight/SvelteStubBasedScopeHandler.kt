// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

object SvelteStubBasedScopeHandler : JSStubBasedScopeHandler() {

    override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean {
        val initialScope = getScope(context)
        return if (initialScope == null)
            processDeclarationsInTemplateScope(context, processor, false)
        else
            super.processDeclarationsInScope(context, processor, includeParentScopes)
            && (!includeParentScopes || processDeclarationsInTemplateScope(context, processor, true))
    }

    private fun processDeclarationsInTemplateScope(context: PsiElement,
                                                   processor: PsiScopeProcessor,
                                                   includeParentScopes: Boolean): Boolean {
        val file = context.containingFile as? SvelteHtmlFile ?: return true

        file.instanceScript
            ?.let { getJsEmbeddedContent(it) }
            ?.let { super.processDeclarationsInScope(it, processor, includeParentScopes) }
            ?.let {
                if (!it || !includeParentScopes)
                    return it
            }
        return file.moduleScript
            ?.let { getJsEmbeddedContent(it) }
            ?.let { super.processDeclarationsInScope(it, processor, includeParentScopes) } != false
    }

}
// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.javascript.psi.resolve.JavaScriptResolveScopeProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

class SvelteJSElementResolveScopeProvider : JavaScriptResolveScopeProvider(), JSElementResolveScopeProvider {
    override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
        if (element.containingFile.viewProvider !is SvelteFileViewProvider) return null

        val project = element.containingFile.project
        val virtualFile = element.containingFile.virtualFile

        val predefinedLibraryFiles = this.getPredefinedLibraryFiles(project)
        return GlobalSearchScope.filesScope(project, predefinedLibraryFiles).uniteWith(GlobalSearchScope.fileScope(project, virtualFile))
    }
}

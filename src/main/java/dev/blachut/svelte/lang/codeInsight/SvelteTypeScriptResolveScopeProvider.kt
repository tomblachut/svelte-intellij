// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveScopeProvider
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import dev.blachut.svelte.lang.SvelteFileType

class SvelteTypeScriptResolveScopeProvider : JSElementResolveScopeProvider {
  private val tsProvider = object : TypeScriptResolveScopeProvider() {
    override fun isApplicable(file: VirtualFile): Boolean = true

    override fun restrictByFileType(file: VirtualFile,
                                    libraryService: TypeScriptLibraryProvider,
                                    moduleAndLibraryScope: GlobalSearchScope): GlobalSearchScope {
      return super.restrictByFileType(file, libraryService, moduleAndLibraryScope).uniteWith(
        GlobalSearchScope.getScopeRestrictedByFileTypes(moduleAndLibraryScope, SvelteFileType.INSTANCE))
    }
  }

  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    val psiFile = element.containingFile
    if (DialectDetector.isTypeScript(element)) {
      return tsProvider.getResolveScope(psiFile.viewProvider.virtualFile, element.project)
    }
    return null
  }
}

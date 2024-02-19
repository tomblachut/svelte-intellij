// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.config.JSImportResolveContext
import com.intellij.lang.typescript.tsconfig.*
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver.JS_DEFAULT_PRIORITY
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.xml.util.HtmlUtil.LANG_ATTRIBUTE_NAME
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isTSLangValue
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

const val svelteExtension = ".svelte"
val svelteExtensionsWithDot = arrayOf(svelteExtension)

class SvelteTypeScriptImportsResolverProvider : TypeScriptImportsResolverProvider {
  override fun isImplicitTypeScriptFile(project: Project, file: VirtualFile): Boolean {
    if (file.fileType != SvelteHtmlFileType) return false

    val psiFile = PsiManager.getInstance(project).findFile(file) as? SvelteHtmlFile ?: return false
    val langAttr = psiFile.instanceScript?.getAttribute(LANG_ATTRIBUTE_NAME)?.value
    return isTSLangValue(langAttr)
  }

  override fun getExtensions(): Array<String> = svelteExtensionsWithDot

  override fun contributeResolver(project: Project, config: TypeScriptConfig): TypeScriptFileImportsResolver? {
    // TODO check if package.json includes svelte
    return SvelteFileImportsResolverImpl(project, config.resolveContext)
  }

  override fun contributeResolver(project: Project,
                                  context: TypeScriptImportResolveContext,
                                  contextFile: VirtualFile): TypeScriptFileImportsResolver? {
    if (!isSvelteContext(contextFile)) return null

    return SvelteFileImportsResolverImpl(project, context)
  }
}

class SvelteFileImportsResolverImpl(project: Project, resolveContext: JSImportResolveContext)
  : TypeScriptFileImportsResolverImpl(project, resolveContext, svelteExtensionsWithDot, listOf(SvelteHtmlFileType)) {
  override fun getPriority(): Int = JS_DEFAULT_PRIORITY
}

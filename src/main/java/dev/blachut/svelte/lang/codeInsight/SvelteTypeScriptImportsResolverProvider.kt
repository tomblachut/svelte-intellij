// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.typescript.modules.TypeScriptNodeSearchProcessor.TS_PROCESSOR
import com.intellij.lang.typescript.tsconfig.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.isTSLangValue
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

const val svelteExtension = ".svelte"
val svelteExtensionsWithDot = arrayOf(svelteExtension)

class SvelteTypeScriptImportsResolverProvider : TypeScriptImportsResolverProvider {
    override fun isDynamicFile(project: Project, file: VirtualFile): Boolean {
        if (file.fileType != SvelteHtmlFileType.INSTANCE) return false

        val psiFile = PsiManager.getInstance(project).findFile(file) as? SvelteHtmlFile ?: return false
        val langAttr = psiFile.instanceScript?.getAttribute("lang")?.value
        return isTSLangValue(langAttr)
    }

    override fun useExplicitExtension(extensionWithDot: String): Boolean = extensionWithDot == svelteExtension
    override fun getExtensions(): Array<String> = svelteExtensionsWithDot

    override fun contributeResolver(project: Project, config: TypeScriptConfig): TypeScriptFileImportsResolver? {
        // TODO check if package.json includes svelte
        return TypeScriptFileImportsResolverImpl(project, config.resolveContext, TS_PROCESSOR, svelteExtensionsWithDot, listOf(SvelteHtmlFileType.INSTANCE))
    }

    override fun contributeResolver(project: Project,
                                    context: TypeScriptImportResolveContext,
                                    contextFile: VirtualFile): TypeScriptFileImportsResolver? {
        if (!isSvelteContext(contextFile)) return null

        return TypeScriptFileImportsResolverImpl(project, context, TS_PROCESSOR, svelteExtensionsWithDot, listOf(SvelteHtmlFileType.INSTANCE))
    }
}

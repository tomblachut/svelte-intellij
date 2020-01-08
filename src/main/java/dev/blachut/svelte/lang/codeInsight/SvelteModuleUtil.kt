package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6FromClause
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathBuilder
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfigurationImpl
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.JSModuleNameInfoImpl
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.stubs.ES6ExportedNamesIndex
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.getRelativePath

object SvelteModuleUtil {
    fun getModuleInfos(project: Project, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String): MutableList<JSModuleNameInfo> {
        val infos = mutableListOf<JSModuleNameInfo>()
        val exports: Collection<JSElement> = StubIndex.getElements(ES6ExportedNamesIndex.KEY, componentName, project, GlobalSearchScope.allScope(project), JSElement::class.java)
        exports.forEach {
            if (it is ES6ExportSpecifier) {
                val declaration = it.declaration ?: return@forEach
                val from: ES6FromClause = PsiTreeUtil.findChildOfType(declaration, ES6FromClause::class.java)
                    ?: return@forEach
                val component = from.resolveReferencedElements().find { referencedFile ->
                    referencedFile is PsiFile
                        && referencedFile.viewProvider is SvelteFileViewProvider
                        && referencedFile.virtualFile == componentVirtualFile
                }
                component ?: return@forEach
                val moduleVirtualFile = declaration.containingFile.virtualFile
                val configuration = JSImportPathConfigurationImpl(currentFile, it, moduleVirtualFile, false)
                infos.add(ES6CreateImportUtil.getExternalFileModuleName(JSImportPathBuilder.createBuilder(configuration)))
            }
        }
        infos.add(JSModuleNameInfoImpl(getRelativePath(currentFile.virtualFile, componentVirtualFile), componentVirtualFile, componentVirtualFile, currentFile, arrayOf("svelte"), true))
        return infos
    }
}

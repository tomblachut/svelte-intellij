package dev.blachut.svelte.lang.codeInsight

import com.intellij.javascript.JSModuleBaseReference
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.frameworks.commonjs.CommonJSUtil
import com.intellij.lang.javascript.frameworks.modules.JSResolvableModuleReferenceContributor
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptModule
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.containers.ContainerUtil
import dev.blachut.svelte.lang.isSvelteContext

class SvelteKitModuleReferenceContributor : JSResolvableModuleReferenceContributor() {
    override fun isApplicable(host: PsiElement): Boolean {
        return super.isApplicable(host) && isSvelteContext(host)
    }

    override fun isAcceptableText(unquotedRefText: String): Boolean {
        val requiredModuleName = FileUtilRt.toSystemIndependentName(unquotedRefText)
        return StringUtil.startsWith(requiredModuleName, "\$app/")
    }

    override fun getDefaultWeight(): Int {
        return JSModuleBaseReference.ModuleTypes.TS_MODULE.weight()
    }

    override fun resolveElement(element: PsiElement, text: String): Array<ResolveResult> {
        return findExternalModule(element.containingFile.originalFile, text)
    }

    // based on TypeScriptUtil.findExternalModule
    private fun findExternalModule(refFile: PsiFile, unquotedEscapedModuleText: String): Array<ResolveResult> {
        ApplicationManager.getApplication().assertReadAccessAllowed()

        val quoted = StringUtil.wrapWithDoubleQuote(JSStringUtil.unescapeStringLiteralValue(unquotedEscapedModuleText))
        val unifiedName = CommonJSUtil.unifyModuleName(quoted)
        val scope = createFilterByNodeModuleScope(JSResolveUtil.getResolveScope(refFile), refFile)

        val modules = TypeScriptClassResolver.getInstance().findGlobalElementsByQName(refFile.project, unifiedName, scope)
        if (!modules.isEmpty()) {
            val result: MutableList<ResolveResult> = ArrayList()
            for (module in modules) {
                if (module is TypeScriptModule && !module.isAugmentation) {
                    result.add(JSResolveResult(module))
                }
            }
            if (!ContainerUtil.isEmpty(result)) return result.toTypedArray()
        }
        return ResolveResult.EMPTY_ARRAY
    }

    // based on TypeScriptUtil.createFilterByNodeModuleScope
    private fun createFilterByNodeModuleScope(scope: GlobalSearchScope, context: PsiElement): GlobalSearchScope {
        @Suppress("NAME_SHADOWING")
        var context = context
        if (context is PsiFile) context = context.originalFile
        val contextFile = PsiUtilCore.getVirtualFile(context)
        val index = ProjectFileIndex.getInstance(context.project)
        val resultScope = if (contextFile == null) scope
        else object : DelegatingGlobalSearchScope(scope) {
            override fun contains(file: VirtualFile): Boolean {
                if (!super.contains(file)) return false
                val currentNodeModules = JSLibraryUtil.findAncestorLibraryDir(file, JSLibraryUtil.NODE_MODULES) ?: return true
                val parentDir = currentNodeModules.parent

                //for global libraries and node core libraries
                val isPartOfProject = index.isInContent(parentDir) || index.isInLibrary(parentDir)
                return !isPartOfProject || VfsUtilCore.isAncestor(parentDir, contextFile, true)
            }
        }

        return resultScope
    }
}

package dev.blachut.svelte.lang

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile


class SvelteFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(virtualFile: VirtualFile, language: Language, psiManager: PsiManager, physical: Boolean): FileViewProvider {
        if (virtualFile is LightVirtualFile && virtualFile.extension?.toLowerCase() == "html" && language is SvelteLanguage) {
            // fixes emmet bug #19
            val file = PsiFileFactory.getInstance(psiManager.project)
                    .createFileFromText(virtualFile.name, HTMLLanguage.INSTANCE, virtualFile.content, true, true)
            val vFile = file.virtualFile
            return SvelteFileViewProvider(psiManager, vFile, physical)
        }
        return SvelteFileViewProvider(psiManager, virtualFile, physical)
    }
}

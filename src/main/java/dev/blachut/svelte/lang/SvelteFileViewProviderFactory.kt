package dev.blachut.svelte.lang

import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.PsiManager


class SvelteFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean): FileViewProvider {
        return SvelteFileViewProvider(file, manager, eventSystemEnabled)
    }
}

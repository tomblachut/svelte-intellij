package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import dev.blachut.svelte.lang.SvelteFileType
import dev.blachut.svelte.lang.SvelteLanguage

class SvelteFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SvelteLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return SvelteFileType.INSTANCE
    }

    override fun toString(): String {
        return "Svelte Component"
    }
}

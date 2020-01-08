package dev.blachut.svelte.lang

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider

class SvelteFileViewProvider(virtualFile: VirtualFile, psiManager: PsiManager, eventSystemEnabled: Boolean)
    : SingleRootFileViewProvider(psiManager, virtualFile, eventSystemEnabled, SvelteHTMLLanguage.INSTANCE)

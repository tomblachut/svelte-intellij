package dev.blachut.svelte.lang

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile

fun isSvelteComponentTag(name: String): Boolean {
    // TODO Support namespaced components
    return StringUtil.isCapitalized(name)
}

fun getRelativePath(currentFile: VirtualFile, componentFile: VirtualFile): String {
    return FileUtil.getRelativePath(currentFile.parent.path, componentFile.path, '/') ?: ""
}

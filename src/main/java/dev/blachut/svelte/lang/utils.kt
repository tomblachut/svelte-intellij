package dev.blachut.svelte.lang

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType

fun isSvelteComponentTag(name: String): Boolean {
    // TODO Support namespaced components
    return StringUtil.isCapitalized(name)
}

fun getRelativePath(currentFile: VirtualFile, componentFile: VirtualFile): String {
    return FileUtil.getRelativePath(currentFile.parent.path, componentFile.path, '/') ?: ""
}

fun PsiBuilder.isTokenAfterWhiteSpace(): Boolean {
    // tokenType is called because it skips whitespaces, unlike bare advanceLexer()
    this.tokenType
    val lastRawToken = this.rawLookup(-1)
    return lastRawToken === TokenType.WHITE_SPACE
}

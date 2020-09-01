package dev.blachut.svelte.lang

import com.intellij.lang.javascript.index.IndexedFileTypeProvider
import com.intellij.openapi.fileTypes.FileType

class SvelteIndexedFileTypeProvider : IndexedFileTypeProvider {
    override fun getFileTypesToIndex(): Array<FileType> = arrayOf(SvelteHtmlFileType.INSTANCE)
}

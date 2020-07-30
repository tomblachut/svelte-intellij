package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlFileNSInfoProvider
import dev.blachut.svelte.lang.SvelteHtmlFileType

class SvelteNSInfoProvider : XmlFileNSInfoProvider {
    override fun getDefaultNamespaces(file: XmlFile): Array<Array<String>>? {
        return null
    }

    override fun overrideNamespaceFromDocType(file: XmlFile): Boolean {
        return file.virtualFile?.fileType === SvelteHtmlFileType.INSTANCE
//        return file.fileType === SvelteHtmlFileType.INSTANCE
    }
}

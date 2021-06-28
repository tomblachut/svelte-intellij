package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlUtil
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

fun prepareInstanceScriptContent(containingFile: SvelteHtmlFile): JSEmbeddedContent {
    var instanceScript = containingFile.instanceScript
    if (instanceScript == null) {
        val elementFactory = XmlElementFactory.getInstance(containingFile.project)
        val emptyInstanceScript = elementFactory.createTagFromText("<script>\n</script>", SvelteHTMLLanguage.INSTANCE)
        val moduleScript = containingFile.moduleScript
        val xmlDocument = containingFile.document!!

        instanceScript = WriteAction.compute<XmlTag, Throwable> {
            val createdScript = if (moduleScript != null) {
                xmlDocument.addAfter(emptyInstanceScript, moduleScript) as XmlTag
            }
            else {
                xmlDocument.addBefore(emptyInstanceScript, xmlDocument.firstChild) as XmlTag
            }

            if (createdScript.nextSibling !is PsiWhiteSpace) {
                // todo refine
                // todo add enter between module script and created instance script
                JSChangeUtil.addWsAfter(xmlDocument, createdScript, "\n\n")
            }

            return@compute createdScript
        }!!
    }

    val embeddedContent = getJsEmbeddedContent(instanceScript)
    if (embeddedContent != null) return embeddedContent

    // instanceScript is empty, we need to insert something in order to get JSEmbeddedContent
    WriteAction.run<Throwable> {
        XmlUtil.expandTag(instanceScript)

        val documentManager = PsiDocumentManager.getInstance(containingFile.project)
        val document = documentManager.getDocument(containingFile)!!
        documentManager.doPostponedOperationsAndUnblockDocument(document)
        document.insertString(instanceScript.value.textRange.startOffset, "\n")
        documentManager.commitDocument(document)
    }

    return getJsEmbeddedContent(instanceScript)!!
}

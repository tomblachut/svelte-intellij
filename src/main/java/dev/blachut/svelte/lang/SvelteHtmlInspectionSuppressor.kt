package dev.blachut.svelte.lang

import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import dev.blachut.svelte.lang.directives.SvelteDirectiveSupport

class SvelteHtmlInspectionSuppressor : DefaultXmlSuppressionProvider() {
    private val scriptAttributes = listOf("context")
    private val styleAttributes = listOf("src", "global")

    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (inspectionId == "XmlUnboundNsPrefix") {
            return true
        }

        if (inspectionId == "HtmlUnknownAttribute") {
            val attribute = element.parent
            if (attribute is XmlAttribute) {
                if (SvelteDirectiveSupport.directivePrefixes.contains(attribute.namespacePrefix)) return true

                // TODO refactor into proper descriptors
                if (attribute.parent.name == "script" && scriptAttributes.contains(attribute.name)) return true
                if (attribute.parent.name == "style" && styleAttributes.contains(attribute.name)) return true
            }
        }

        if (inspectionId == "HtmlUnknownTag") {
            if (isSvelteComponentTag(element.text)) {
                return true
            }
        }

        return super.isSuppressedFor(element, inspectionId)
    }

    override fun isProviderAvailable(file: PsiFile): Boolean {
        return isSvelteContext(file)
    }
}

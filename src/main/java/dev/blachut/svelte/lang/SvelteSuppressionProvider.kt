package dev.blachut.svelte.lang

import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute

class SvelteSuppressionProvider : DefaultXmlSuppressionProvider() {
    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (inspectionId == "XmlUnboundNsPrefix") {
            return true
        }

        if (inspectionId == "HtmlUnknownAttribute") {
            val attribute = element.parent
            if (attribute is XmlAttribute) {
                if (directives.contains(attribute.namespacePrefix) || suppressedAttributes.contains(attribute.name)) {
                    return true
                }
            }
        }

        if (inspectionId == "HtmlUnknownTag") {
            if (isSvelteComponentTag(element.text) || element.text == "slot") {
                return true
            }
        }

        return super.isSuppressedFor(element, inspectionId)
    }

    override fun isProviderAvailable(file: PsiFile): Boolean {
        return isSvelteContext(file)
    }
}

val directives = listOf("on", "bind", "class", "use", "transition", "in", "out", "animate", "let")
private val suppressedAttributes = listOf("context")

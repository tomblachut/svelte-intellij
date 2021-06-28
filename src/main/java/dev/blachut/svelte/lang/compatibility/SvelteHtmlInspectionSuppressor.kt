package dev.blachut.svelte.lang.compatibility

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.equalsName
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isSvelteContext

class SvelteHtmlInspectionSuppressor : DefaultXmlSuppressionProvider() {
    private val scriptAttributes = listOf("context")
    private val styleAttributes = listOf("src", "global")

    override fun isProviderAvailable(file: PsiFile): Boolean {
        return isSvelteContext(file)
    }

    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (inspectionId.equalsName<XmlUnboundNsPrefixInspection>()) {
            return true
        }

        if (inspectionId.equalsName<HtmlUnknownAttributeInspection>()) {
            val attribute = element.parent
            if (attribute is XmlAttribute) {
                if (SvelteDirectiveUtil.directivePrefixes.contains(attribute.namespacePrefix)) return true

                // TODO refactor into proper descriptors
                if (attribute.parent.name == "script" && scriptAttributes.contains(attribute.name)) return true
                if (attribute.parent.name == "style" && styleAttributes.contains(attribute.name)) return true
            }
        }

        if (inspectionId.equalsName<HtmlUnknownTagInspection>()) {
            if (isSvelteComponentTag(element.text)) {
                return true
            }
        }

        return super.isSuppressedFor(element, inspectionId)
    }
}

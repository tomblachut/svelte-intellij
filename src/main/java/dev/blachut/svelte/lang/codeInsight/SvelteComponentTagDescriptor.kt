package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementsGroup
import com.intellij.xml.XmlNSDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import org.jetbrains.annotations.NonNls
import java.util.*

/**
 * TODO Consider using BaseXmlElementDescriptorImpl, there are cache implementations there
 *
 * TODO HtmlElementDescriptorImpl is used for example in HtmlUnknownTagInspectionBase: extending it could help or hinder code insight
 */
class SvelteComponentTagDescriptor(private val myName: String, private val myTag: SvelteHtmlTag) :
    XmlElementDescriptor {
    override fun getName(context: PsiElement): String = name

    override fun getName(): String = myName

    override fun getQualifiedName(): String = myName

    override fun getDefaultName(): String = myName

    override fun getDeclaration(): SvelteHtmlTag = myTag

    override fun getAttributesDescriptors(context: XmlTag?): Array<XmlAttributeDescriptor> {
        if (context == null) {
            return XmlAttributeDescriptor.EMPTY
        }

        val import = myTag.reference?.resolve()
        if (import is ES6ImportedBinding && !import.isNamespaceImport) {
            // com.intellij.javascript.JSFileReference.IMPLICIT_EXTENSIONS doesn't include .svelte
            // probably because of that following call returns null
            // val componentFile = declaration.findReferencedElements().firstOrNull()
            val componentFile = import.declaration?.fromClause?.resolveReferencedElements()?.firstOrNull()

            if (componentFile is SvelteHtmlFile && isSvelteContext(componentFile)) {
                val props = SveltePropsProvider.getComponentProps(componentFile.viewProvider)
                if (props != null) {
                    return knownAttributeDescriptors + props.map { AnyXmlAttributeDescriptor(it) }
                }
            }
        }

        return knownAttributeDescriptors
    }

    override fun getAttributeDescriptor(attribute: XmlAttribute): XmlAttributeDescriptor? {
        return getAttributeDescriptor(attribute.name, attribute.parent)
    }

    override fun getAttributeDescriptor(@NonNls attributeName: String, context: XmlTag?): XmlAttributeDescriptor? {
        var descriptor = attributeDescriptorCache[attributeName]
        if (descriptor == null) {
            descriptor = AnyXmlAttributeDescriptor(attributeName)
            attributeDescriptorCache[descriptor.name] = descriptor
        }

        return descriptor
    }

    override fun getContentType(): Int = XmlElementDescriptor.CONTENT_TYPE_ANY

    override fun getElementsDescriptors(context: XmlTag): Array<XmlElementDescriptor> {
        return XmlDescriptorUtil.getElementsDescriptors(context)
    }

    override fun getElementDescriptor(childTag: XmlTag, contextTag: XmlTag): XmlElementDescriptor? {
        return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
    }

    override fun getNSDescriptor(): XmlNSDescriptor? = null

    override fun getTopGroup(): XmlElementsGroup? = null

    override fun getDefaultValue(): String? = null

    override fun init(element: PsiElement) {}

    companion object {
        private val slotDescriptor = AnyXmlAttributeDescriptor("slot")
        private val knownAttributeDescriptors = arrayOf<XmlAttributeDescriptor>(slotDescriptor)
        private val attributeDescriptorCache = HashMap<String, XmlAttributeDescriptor>()

        init {
            attributeDescriptorCache[slotDescriptor.name] = slotDescriptor
        }
    }
}

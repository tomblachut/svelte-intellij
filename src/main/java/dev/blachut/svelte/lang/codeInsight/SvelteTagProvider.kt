package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.completion.SvelteInsertHandler
import dev.blachut.svelte.lang.icons.SvelteIcons
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

// Vue plugin uses 100, it's ok for now
const val highPriority = 100.0
const val mediumPriority = 50.0

const val svelteNamespace = "svelte"
const val sveltePrefix = "$svelteNamespace:"

val svelteTagNames = arrayOf("self", "component", "window", "body", "head", "options")

// TODO Merge with svelteBareTagLookupElements
// TODO Use XmlTagInsertHandler
val svelteNamespaceTagLookupElements = svelteTagNames.map {
    LookupElementBuilder.create(sveltePrefix + it).withIcon(SvelteIcons.GRAY)
}

val slotLookupElement: LookupElementBuilder = LookupElementBuilder.create("slot").withIcon(SvelteIcons.GRAY)
    .withInsertHandler(XmlTagInsertHandler.INSTANCE)

/**
 * When user auto completes after writing colon in "svelte", editor will produce i.e. "svelte:svelte:self".
 */
val svelteBareTagLookupElements = svelteTagNames.map {
    val lookupElement = LookupElementBuilder.create(it).withIcon(SvelteIcons.GRAY)
    PrioritizedLookupElement.withPriority(lookupElement, mediumPriority)
}

/**
 * interface XmlTagNameProvider feeds data for name completion popup
 *
 * interface XmlElementDescriptorProvider enables, among others, navigation from tag to component file
 */
class SvelteTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
    override fun getDescriptor(tag: XmlTag): XmlElementDescriptor? {
        if (tag !is SvelteHtmlTag) return null

        if (tag.namespacePrefix == svelteNamespace && svelteTagNames.contains(tag.localName) || tag.name == "slot") {
            return SvelteElementDescriptor(tag)
        }

        if (!isSvelteComponentTag(tag.name)) return null

        return SvelteComponentTagDescriptor(tag.name, tag)
    }

    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
        if (tag !is SvelteHtmlTag) return

        if (svelteNamespace == namespacePrefix) {
            elements.addAll(svelteBareTagLookupElements)
        } else if (namespacePrefix.isEmpty()) {
            elements.addAll(svelteNamespaceTagLookupElements)
            elements.add(slotLookupElement)

            // TODO Link component documentation
            elements.addAll(getReachableComponents(tag))
        }
    }

    private fun getReachableComponents(tag: SvelteHtmlTag): List<LookupElement> {
        val lookupElements = mutableListOf<LookupElement>()
        val svelteVirtualFiles = FileTypeIndex.getFiles(
            SvelteHtmlFileType.INSTANCE,
            GlobalSearchScope.allScope(tag.project)
        )

        svelteVirtualFiles.forEach { virtualFile ->
            val componentName = virtualFile.nameWithoutExtension

            if (!isSvelteComponentTag(componentName)) return@forEach

            val moduleInfos = SvelteModuleUtil.getModuleInfos(
                tag.project,
                tag.containingFile.originalFile,
                virtualFile,
                componentName
            )
            val typeText = " (${virtualFile.name})"

            for (info in moduleInfos) {
                val lookupObject = ComponentLookupObject(virtualFile, info)
                val lookupElement = LookupElementBuilder.create(lookupObject, componentName)
                    .withIcon(info.resolvedFile.fileType.icon)
                    .withTailText(typeText, true)
                    .withInsertHandler(SvelteInsertHandler)
                    .let { PrioritizedLookupElement.withPriority(it, highPriority) }

                lookupElements.add(lookupElement)
            }
        }

        // TODO Include imported & re-exported components
        return lookupElements
    }
}

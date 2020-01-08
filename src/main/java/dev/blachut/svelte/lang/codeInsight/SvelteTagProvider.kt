package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.completion.SvelteInsertHandler
import dev.blachut.svelte.lang.icons.SvelteIcons
import dev.blachut.svelte.lang.isSvelteComponentTag

// Vue plugin uses 100, it's ok for now
const val highPriority = 100.0
const val mediumPriority = 50.0

const val svelteNamespace = "svelte"
const val sveltePrefix = "$svelteNamespace:"

val svelteTagNames = arrayOf("self", "component", "window", "body", "head", "options")

// TODO Merge with svelteBareTagLookupElements
val svelteNamespacedTagLookupElements = svelteTagNames.map {
    LookupElementBuilder.create(sveltePrefix + it).withIcon(SvelteIcons.FILE)
}

/**
 * When user auto completes after writing colon in "svelte", editor will produce i.e. "svelte:svelte:self".
 * I'm clearly missing something, but for now, let it be.
 */
val svelteBareTagLookupElements = svelteTagNames.map {
    val lookupElement = LookupElementBuilder.create(it).withIcon(SvelteIcons.FILE)
    PrioritizedLookupElement.withPriority(lookupElement, mediumPriority)
}

/**
 * interface XmlTagNameProvider feeds data for name completion popup
 * interface XmlElementDescriptorProvider enables, among others, navigation from tag to component file
 */
class SvelteTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
    override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
        if (tag == null || tag.containingFile.viewProvider !is SvelteFileViewProvider) return null
        if (!isSvelteComponentTag(tag.name)) return null

        val result = SvelteTagReference(tag).resolve()

        if (result !is JSElement) return null

        // TODO Look into caching SvelteComponentTagDescriptor in CachedValuesManager
        return SvelteComponentTagDescriptor(tag.name, result)
    }

    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
        if (tag !is HtmlTag || tag.containingFile.viewProvider !is SvelteFileViewProvider) return

        if (svelteNamespace == namespacePrefix) {
            elements.addAll(svelteBareTagLookupElements)
            // in svelte there are no custom components to scan so early return
            return
        } else if (namespacePrefix.isNotEmpty()) {
            // early return for namespaces other than svelte
            return
        } else {
            elements.addAll(svelteNamespacedTagLookupElements)
        }

        // TODO Link component documentation
        elements.addAll(getReachableComponents(tag))
    }

    private fun getReachableComponents(tag: HtmlTag): List<LookupElement> {
        val lookupElements = mutableListOf<LookupElement>()
        val svelteVirtualFiles = FileTypeIndex.getFiles(SvelteHtmlFileType.INSTANCE, GlobalSearchScope.allScope(tag.project))

        svelteVirtualFiles.forEach { virtualFile ->
            val componentName = virtualFile.nameWithoutExtension

            if (!isSvelteComponentTag(componentName)) return@forEach

            val moduleInfos = SvelteModuleUtil.getModuleInfos(tag.project, tag.containingFile.originalFile, virtualFile, componentName)
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

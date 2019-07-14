package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.SvelteFileType
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.completion.SvelteInsertHandler
import dev.blachut.svelte.lang.icons.SvelteIcons

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
 * When user autocompletes after writing colon in "svelte", editor will produce i.e. "svelte:svelte:self".
 * I'm clearly missing something, but for now, let it be.
 */
val svelteBareTagLookupElements = svelteTagNames.map {
    val lookupElement = LookupElementBuilder.create(it).withIcon(SvelteIcons.FILE)
    PrioritizedLookupElement.withPriority(lookupElement, mediumPriority)
}

/**
 * interface XmlTagNameProvider feeds data for name completion dropdown
 * interface XmlElementDescriptorProvider enables, among others, navigation from tag to component file
 */
class SvelteTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
    override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
        if (tag == null || tag.containingFile.viewProvider !is SvelteFileViewProvider) return null

        val file = tag.containingFile
        val visitor = SvelteScriptVisitor()
        file.accept(visitor)
        val jsElement = visitor.jsElement ?: return null

        val importVisitor = ImportVisitor()
        jsElement.accept(importVisitor)
        val binding = importVisitor.bindings.find { it.name == tag.name } ?: return null

        // TODO Support reexports
        // TODO Look into caching SvelteComponentTagDescriptor in CachedValuesManager
        return SvelteComponentTagDescriptor(tag.name, binding)
    }

    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
        if (tag !is HtmlTag || tag.containingFile.viewProvider !is SvelteFileViewProvider) return

        if (svelteNamespace == namespacePrefix) {
            elements.addAll(svelteBareTagLookupElements)
            // in svelte there are no custom components to scan so early return
            return
        } else if (!namespacePrefix.isEmpty()) {
            // early return for namespaces other than svelte
            return
        } else {
            elements.addAll(svelteNamespacedTagLookupElements)
        }

        val svelteFiles = FileTypeIndex.getFiles(SvelteFileType.INSTANCE, GlobalSearchScope.allScope(tag.project))
        val reachableComponents = svelteFiles.map {
            val componentName = it.nameWithoutExtension
            val componentProps = ComponentPropsProvider().getComponentProps(it, tag.project)

            val lookupObject = mapOf("file" to it, "props" to componentProps)
            var lookupElement = LookupElementBuilder.create(lookupObject, componentName)
                    .withIcon(SvelteIcons.FILE)
                    .withInsertHandler(SvelteInsertHandler.INSTANCE)

            if (componentProps != null) {
                val joinedProps = componentProps.map { prop -> "$prop={...}" }.joinToString(" ").trim()
                val typeText = "<$componentName $joinedProps>"
                lookupElement = lookupElement.withTypeText(typeText, true)
            }
            PrioritizedLookupElement.withPriority(lookupElement, highPriority)
        }

        // TODO Link component documentation
        // TODO Include svelte internal components
        elements.addAll(reachableComponents)
    }
}

package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import dev.blachut.svelte.lang.icons.SvelteIcons

/**
 * interface XmlTagNameProvider feeds data for name completion dropdown
 * interface XmlElementDescriptorProvider enables, among others, navigation from tag to component file
 */
class SvelteTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
    override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
        if (tag == null || tag.containingFile.language != HTMLLanguage.INSTANCE) return null

        val file = tag.containingFile
        val visitor = SvelteScriptVisitor()
        file.accept(visitor)
        val jsElement = visitor.jsElement ?: return null

        val importVisitor = ImportVisitor()
        jsElement.accept(importVisitor)
        val binding = importVisitor.bindings.find { it.name == tag.name } ?: return null

        // TODO Support reexports
        // TODO Look into caching SvelteTagDescriptor in CachedValuesManager
        return SvelteTagDescriptor(tag.name, binding)
    }

    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, namespacePrefix: String) {
        if (!StringUtil.isEmpty(namespacePrefix) && tag !is HtmlTag) return

        val file = tag.containingFile
        val visitor = SvelteScriptVisitor()
        file.accept(visitor)
        val jsElement = visitor.jsElement ?: return

        val importVisitor = ImportVisitor()
        jsElement.accept(importVisitor)
        val items = importVisitor.components.map {
            val lookupElement = LookupElementBuilder.create(it).withIcon(SvelteIcons.FILE)
            // Vue uses 100, it's ok for now
            PrioritizedLookupElement.withPriority(lookupElement, 100.0)
        }
        // TODO Link component documentation
        // TODO Include svelte internal components
        // TODO Include not-imported reachable components and enable auto-import
        elements.addAll(items)
    }
}


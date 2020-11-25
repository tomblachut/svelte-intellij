package dev.blachut.svelte.lang.codeInsight

import com.intellij.html.impl.providers.HtmlAttributeValueProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.directives.SvelteDirectiveTypes
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

class SvelteAttributeValueProvider : HtmlAttributeValueProvider() {
    override fun getCustomAttributeValues(tag: XmlTag?, attributeName: String?): String? {
        if (tag !is SvelteHtmlTag) return null
        if (!HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attributeName, ignoreCase = true)) return null

        val directiveClasses = SmartList<String>()
        var nativeClass: String? = null
        for (attribute in tag.attributes) {
            if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attribute.name, ignoreCase = true)) {
                nativeClass = attribute.value
            }

            if (attribute !is SvelteHtmlAttribute) continue

            val directive = attribute.directive
            if (directive != null && directive.directiveType == SvelteDirectiveTypes.CLASS) {
                directiveClasses.add(directive.specifiers[0].text)
            }
        }

        if (directiveClasses.size == 0) return null

        nativeClass?.let(directiveClasses::add)
        return directiveClasses.joinToString(" ")
    }
}

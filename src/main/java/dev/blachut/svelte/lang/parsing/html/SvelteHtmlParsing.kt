package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteHtmlParsing(builder: PsiBuilder) : HtmlParsing(builder) {
    override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
        // Inspired by Vue plugin. Svelte tags must be closed explicitly
        if (isSvelteComponentTag(originalTagName)) {
            return false
        }
        return super.isSingleTag(tagName, originalTagName)
    }
}

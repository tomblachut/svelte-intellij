package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_NAME).withParent(XmlPatterns.xmlAttribute()).inFile(psiFile(SvelteHtmlFile::class.java)),
            SvelteAttributeNameCompletionProvider())
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inFile(psiFile(SvelteHtmlFile::class.java)),
            SvelteAttributeValueCompletionProvider())
    }
}

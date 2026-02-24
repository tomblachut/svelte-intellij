package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.completion.JSPatternBasedCompletionContributor.REFERENCE_EXPRESSION_PATTERN
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

class SvelteCompletionContributor : CompletionContributor() {
  init {
    extend(
      CompletionType.BASIC,
      psiElement(XmlTokenType.XML_NAME).withParent(XmlPatterns.xmlAttribute()).inFile(psiFile(SvelteHtmlFile::class.java)),
      SvelteAttributeNameCompletionProvider()
    )
    extend(
      CompletionType.BASIC,
      psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inFile(psiFile(SvelteHtmlFile::class.java)),
      SvelteAttributeValueCompletionProvider()
    )
    extend(
      CompletionType.BASIC,
      psiElement(JSTokenTypes.IDENTIFIER)
        .withAncestor(2, psiElement(SvelteJSLazyElementTypes.CONTENT_EXPRESSION)).inFile(psiFile(SvelteHtmlFile::class.java)),
      SvelteKeywordCompletionProvider()
    )
    // Completion for {@attach} in attribute position
    for (elementType in listOf(
      SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND, SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND_TS,
      SvelteJSLazyElementTypes.ATTACH_EXPRESSION, SvelteJSLazyElementTypes.ATTACH_EXPRESSION_TS,
    )) {
      extend(
        CompletionType.BASIC,
        psiElement(JSTokenTypes.IDENTIFIER)
          .withAncestor(2, psiElement(elementType)).inFile(psiFile(SvelteHtmlFile::class.java)),
        SvelteAttachCompletionProvider()
      )
    }
    extend(
      CompletionType.BASIC,
      REFERENCE_EXPRESSION_PATTERN.inFile(psiFile(SvelteHtmlFile::class.java)),
      SvelteReferenceCompletionProvider()
    )
  }
}

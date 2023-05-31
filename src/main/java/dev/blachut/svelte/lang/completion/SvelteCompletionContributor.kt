package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

class SvelteCompletionContributor : CompletionContributor() {
  init {
    // from JSPatternBasedCompletionContributor
    val referencePattern = psiElement(JSTokenTypes.IDENTIFIER)
      .withParent(object : PsiElementPattern.Capture<JSReferenceExpression>(JSReferenceExpression::class.java) {
        override fun accepts(o: Any?, context: ProcessingContext): Boolean {
          if (!super.accepts(o, context)) return false
          assert(o is JSReferenceExpression)
          val parent = (o as JSReferenceExpression?)!!.parent
          return !(parent is ES6Property && parent.isShorthanded)
        }
      })
      .inFile(psiFile(SvelteHtmlFile::class.java))

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
    extend(
      CompletionType.BASIC,
      referencePattern,
      SvelteReactiveDeclarationsCompletionProvider()
    )
  }
}

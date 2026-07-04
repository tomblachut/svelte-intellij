package com.intellij.svelte.css

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.css.CssElement
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtilCore
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference
import dev.blachut.svelte.lang.css.SvelteCssSupport
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

internal class SvelteCssSupportImpl : SvelteCssSupport {
  override fun getClassReference(attribute: SvelteHtmlAttribute, rangeInElement: TextRange): PsiReference? {
    val descriptorProvider = CssDescriptorsUtilCore.findDescriptorProvider(attribute) ?: return null
    return descriptorProvider.getStyleReference(attribute, rangeInElement.startOffset, rangeInElement.endOffset, true)
  }

  override fun addClassCompletions(attribute: SvelteHtmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) {
    val directive = attribute.directive ?: return
    var reference =
      attribute.findReferenceAt(directive.specifiers[0].rangeInName.startOffset) as? HtmlCssClassOrIdReference

    if (reference == null) {
      val rangeInElement = directive.specifiers[0].rangeInName
      val descriptorProvider = CssDescriptorsUtilCore.findDescriptorProvider(attribute) ?: return
      reference = descriptorProvider.getStyleReference(
        attribute,
        rangeInElement.startOffset,
        rangeInElement.endOffset,
        true
      ) as? HtmlCssClassOrIdReference
    }

    if (reference != null) {
      val prefixMatcher = result.prefixMatcher
      reference.addCompletions(parameters, prefixMatcher, result::addElement)
    }
  }

  override fun isCssElement(element: PsiElement): Boolean = element is CssElement

  override fun getStyleDialectLanguages(): List<Language> = CSSLanguage.INSTANCE.dialects
}

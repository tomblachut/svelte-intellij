package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor
import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathReferenceProvider
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.filters.position.FilterPattern

class SveltePathReferenceContributor: PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(creatPathAttributeValuePattern(), PATH_REFERENCE_PROVIDER)
  }
}

private val PATH_REFERENCE_PROVIDER = createPathReferenceProvider()

private fun creatPathAttributeValuePattern(): XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue("href", "to")
  .withSuperParent(2, XmlPatterns.xmlTag().and(FilterPattern(JSXReferenceContributor.createPathContainedTagFilter(false))))

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.css.CssReferenceContributor
import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.xml.util.XmlUtil
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

internal class SvelteCssReferenceContributor : CssReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    val provider = CssInHtmlClassOrIdReferenceProvider()
    XmlUtil.registerXmlAttributeValueReferenceProvider(
      registrar,
      arrayOf("class", "id"),
      SvelteElementClassOrIdFilter,
      false,
      provider,
    )
  }

  private object SvelteElementClassOrIdFilter : ElementFilter {
    override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
      val value = element as? XmlAttributeValue ?: return false
      if (value.containingFile !is SvelteHtmlFile) return false
      val attribute = value.parent as? XmlAttribute ?: return false
      if (attribute.name != "class" && attribute.name != "id") return false
      val tag = attribute.parent ?: return false
      return tag.name == "svelte:element" || tag.name == "svelte:self"
    }

    override fun isClassAcceptable(hintClass: Class<*>?): Boolean = true
  }
}

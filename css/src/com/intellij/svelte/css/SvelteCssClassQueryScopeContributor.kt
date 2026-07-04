// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.svelte.css

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSConditionalExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.javascript.backend.css.polySymbols.CssClassListInJSLiteralInHtmlAttributeScope
import com.intellij.polySymbols.query.PolySymbolQueryScopeContributor
import com.intellij.polySymbols.query.PolySymbolQueryScopeProviderRegistrar
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isSvelteNamespacedComponentTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyPsiElement

class SvelteCssClassQueryScopeContributor : PolySymbolQueryScopeContributor {

  override fun registerProviders(registrar: PolySymbolQueryScopeProviderRegistrar) {
    registrar.apply {
      forPsiLocation(JSLiteralExpression::class.java)
        .contributeScopeProvider { listOfNotNull(getCssClassesScope(it)) }

      forPsiLocation(JSObjectLiteralExpression::class.java)
        .contributeScopeProvider { listOfNotNull(getCssClassesScope(it)) }
    }
  }

  private fun getCssClassesScope(element: PsiElement): PolySymbolScope? =
    element.takeIf { isSvelteClassExpressionContext(it) }
      ?.parentOfType<XmlAttribute>()
      ?.let { CssClassListInJSLiteralInHtmlAttributeScope(it) }
}

private fun isSvelteClassExpressionContext(element: PsiElement): Boolean =
  element.parentOfType<XmlAttribute>()?.let { isSvelteClassAttribute(it) } == true
  && isSvelteExtractedClassExpressionContext(element)

private fun isSvelteExtractedClassExpressionContext(element: PsiElement): Boolean {
  var child = element
  if (!isSvelteClassNameSource(child)) return false

  var parent = child.parent
  while (parent != null) {
    if (parent is SvelteJSLazyPsiElement) return true
    if (!isSvelteSupportedClassExpressionParent(parent, child)) return false

    child = parent
    parent = child.parent
  }

  return false
}

private fun isSvelteClassNameSource(element: PsiElement): Boolean =
  element is JSObjectLiteralExpression
  || element is JSReferenceExpression
  || element is JSLiteralExpression && element.isQuotedLiteral

private fun isSvelteSupportedClassExpressionParent(parent: PsiElement, child: PsiElement): Boolean =
  when (parent) {
    is JSArrayLiteralExpression -> true
    is JSConditionalExpression -> child == parent.thenBranch || child == parent.elseBranch
    is JSProperty -> child == parent.nameIdentifier
    is JSObjectLiteralExpression -> child is JSProperty
    else -> false
  }

internal fun isSvelteClassAttribute(attr: XmlAttribute): Boolean {
  if (attr.name != "class") return false
  val tag = attr.parent ?: return false
  return isSvelteClassAttributeTag(tag)
}

internal fun isSvelteClassAttributeTag(tag: XmlTag): Boolean = when (tag.name) {
  "svelte:element", "svelte:self" -> true
  "svelte:component" -> false
  else -> !tag.name.startsWith("svelte:")
          && !isSvelteComponentTag(tag.name)
          && !isSvelteNamespacedComponentTag(tag.name)
}

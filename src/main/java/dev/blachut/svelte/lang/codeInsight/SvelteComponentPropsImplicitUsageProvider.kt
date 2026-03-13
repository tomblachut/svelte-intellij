// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.isSvelteProjectContext

/**
 * Suppresses "Unused property" warnings for properties in Svelte component props objects.
 *
 * When SPTE is enabled, the unused-symbols inspection may not trace that a property is
 * consumed by the component's Props interface, resulting in false "Unused property" warnings.
 * This provider marks such properties as implicitly used when they're inside a `props` object
 * being passed to `mount()`, `hydrate()`, or `new Component()` in a Svelte project.
 */
class SvelteComponentPropsImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (element !is JSProperty) return false
    if (!isSvelteProjectContext(element)) return false

    // property → object literal → "props:" property → outer object
    val propsObject = element.parent as? JSObjectLiteralExpression ?: return false
    val propsProperty = propsObject.parent as? JSProperty ?: return false
    if (propsProperty.name != "props") return false

    val outerObject = propsProperty.parent as? JSObjectLiteralExpression ?: return false
    return isMountHydrateOrConstructorCall(outerObject)
  }

  private fun isMountHydrateOrConstructorCall(optionsObject: JSObjectLiteralExpression): Boolean {
    // mount(Component, { props: ... }) or hydrate(Component, { props: ... })
    val callExpression = optionsObject.parentOfType<JSCallExpression>()
    if (callExpression != null) {
      val methodRef = callExpression.methodExpression as? JSReferenceExpression
      val name = methodRef?.referenceName
      if (name == "mount" || name == "hydrate") {
        return true
      }
    }

    // new Component({ props: ... })
    if (optionsObject.parentOfType<JSNewExpression>() != null) {
      return true
    }

    return false
  }

  override fun isImplicitRead(element: PsiElement): Boolean = false
  override fun isImplicitWrite(element: PsiElement): Boolean = false
}

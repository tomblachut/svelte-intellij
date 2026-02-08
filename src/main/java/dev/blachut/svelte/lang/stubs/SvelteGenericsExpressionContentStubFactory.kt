// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContent
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContentElementType
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

/**
 * Stub factory for the generics attribute embedded content.
 */
internal class SvelteGenericsExpressionContentStubFactory(
  elementTypeSupplier: () -> SvelteGenericsExpressionContentElementType = { SvelteJSElementTypes.GENERICS_EXPRESSION_CONTENT },
) : JSStubFactory<SvelteGenericsExpressionContentStub, SvelteGenericsExpressionContent>(elementTypeSupplier) {
  override fun createStub(
    psi: SvelteGenericsExpressionContent,
    parentStub: StubElement<out PsiElement>?,
  ): SvelteGenericsExpressionContentStub =
    SvelteGenericsExpressionContentStubImpl(psi, parentStub, elementType)
}

// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.stubs.impl.JSStubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContent
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContentImpl

/**
 * Stub implementation for the generics attribute embedded content.
 */
class SvelteGenericsExpressionContentStubImpl :
  JSStubBase<SvelteGenericsExpressionContent>,
  SvelteGenericsExpressionContentStub {

  constructor(
    psi: SvelteGenericsExpressionContent,
    parent: StubElement<*>?,
    elementType: IElementType,
  ) : super(psi, parent, elementType)

  constructor(
    dataStream: StubInputStream,
    parent: StubElement<*>?,
    elementType: IElementType,
  ) : super(dataStream, parent, elementType)

  override fun createPsi(): SvelteGenericsExpressionContent {
    return SvelteGenericsExpressionContentImpl(this, elementType)
  }
}

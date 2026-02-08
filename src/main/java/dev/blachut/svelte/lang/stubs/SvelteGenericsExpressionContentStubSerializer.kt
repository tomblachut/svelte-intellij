// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContent
import dev.blachut.svelte.lang.psi.SvelteGenericsExpressionContentElementType
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

/**
 * Stub serializer for the generics attribute embedded content.
 */
internal class SvelteGenericsExpressionContentStubSerializer(
  elementTypeSupplier: () -> SvelteGenericsExpressionContentElementType = { SvelteJSElementTypes.GENERICS_EXPRESSION_CONTENT },
) : JSStubSerializer<SvelteGenericsExpressionContentStub, SvelteGenericsExpressionContent>(elementTypeSupplier) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SvelteGenericsExpressionContentStub =
    SvelteGenericsExpressionContentStubImpl(dataStream, parentStub, elementType)
}

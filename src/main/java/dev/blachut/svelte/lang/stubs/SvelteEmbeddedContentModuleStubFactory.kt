package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.stubs.factories.JSEmbeddedContentStubFactory
import dev.blachut.svelte.lang.psi.SvelteJSEmbeddedContentImpl

internal class SvelteEmbeddedContentModuleStubFactory(elementTypeSupplier: () -> JSElementType<JSEmbeddedContent>)
  : JSEmbeddedContentStubFactory(elementTypeSupplier) {
  override fun createPsi(stub: JSEmbeddedContentStub): SvelteJSEmbeddedContentImpl = SvelteJSEmbeddedContentImpl(stub, elementType)
}
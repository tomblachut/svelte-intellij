package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.stubs.factories.JSEmbeddedContentStubFactory
import dev.blachut.svelte.lang.psi.SvelteJSEmbeddedContentImpl

class SvelteEmbeddedContentModuleStubFactory(elementType: JSElementType<JSEmbeddedContent>) : JSEmbeddedContentStubFactory(elementType) {
  override fun createPsi(stub: JSEmbeddedContentStub): SvelteJSEmbeddedContentImpl =
    SvelteJSEmbeddedContentImpl(stub, elementType)
}
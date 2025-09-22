package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.psi.stubs.impl.JSParameterStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.PARAMETER

internal class SvelteParameterStubSerializer : JSStubSerializer<JSParameterStub, JSParameter>({ PARAMETER }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSParameterStub =
    JSParameterStubImpl(dataStream, parentStub, elementType)
}
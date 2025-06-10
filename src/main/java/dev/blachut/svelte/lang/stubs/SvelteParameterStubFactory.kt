package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.psi.stubs.impl.JSParameterStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.PARAMETER
import dev.blachut.svelte.lang.psi.SvelteJSParameter

class SvelteParameterStubFactory : JSStubFactory<JSParameterStub, JSParameter>(PARAMETER) {
  override fun createStub(psi: JSParameter, parentStub: StubElement<out PsiElement>?): JSParameterStub =
    JSParameterStubImpl(psi, parentStub, elementType, 0)

  override fun createPsi(stub: JSParameterStub): JSParameter =
    SvelteJSParameter(stub, elementType)
}
// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.stubs.TypeScriptTypeParameterListStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.SCRIPT_GENERICS_TYPE_PARAMETER_LIST

/**
 * Stub serializer for the type parameter list from the generics attribute.
 */
internal class SvelteGenericsTypeParameterListStubSerializer :
  JSStubSerializer<TypeScriptTypeParameterListStub, TypeScriptTypeParameterList>({ SCRIPT_GENERICS_TYPE_PARAMETER_LIST }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): TypeScriptTypeParameterListStub =
    SvelteGenericsTypeParameterListStubImpl(dataStream, parentStub, elementType)
}

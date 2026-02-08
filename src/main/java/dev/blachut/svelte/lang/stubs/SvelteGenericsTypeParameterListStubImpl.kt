// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeParameterListImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSStubBase
import com.intellij.lang.typescript.TypeScriptElementTypes
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

/**
 * Stub implementation for the type parameter list from the generics attribute.
 */
class SvelteGenericsTypeParameterListStubImpl :
  JSStubBase<TypeScriptTypeParameterList>,
  SvelteGenericsTypeParameterListStub {

  constructor(
    psi: TypeScriptTypeParameterList?,
    parent: StubElement<*>?,
    elementType: JSElementType<*>,
  ) : super(psi!!, parent, elementType)

  constructor(
    dataStream: StubInputStream?,
    parent: StubElement<*>?,
    elementType: JSElementType<*>,
  ) : super(dataStream!!, parent, elementType)

  override fun createPsi(): TypeScriptTypeParameterList {
    return TypeScriptTypeParameterListImpl(this, TypeScriptElementTypes.TYPE_PARAMETER_LIST)
  }
}

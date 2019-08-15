// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType

class SvelteJSParserDefinition : ECMA6ParserDefinition() {
    private val iFileElementType: IFileElementType = JSFileElementType.create(SvelteJSLanguage.INSTANCE)

    override fun getFileNodeType(): IFileElementType {
        return iFileElementType
    }
}

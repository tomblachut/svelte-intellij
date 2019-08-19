package dev.blachut.svelte.lang

import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType

class SvelteJSParserDefinition : ECMA6ParserDefinition() {
    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    companion object {
        val FILE: IFileElementType = JSFileElementType.create(SvelteJSLanguage.INSTANCE)
    }
}

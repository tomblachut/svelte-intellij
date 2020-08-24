package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

class SvelteJSPsiBuilder(delegate: PsiBuilder) : ElementTypeRemappingPsiBuilder(delegate) {
    override fun remapElementType(type: IElementType): IElementType {
        return if (type === JSElementTypes.REFERENCE_EXPRESSION) {
            SvelteJSElementTypes.REFERENCE_EXPRESSION
        } else {
            type
        }
    }
}

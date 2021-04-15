package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.psi.SvelteElementTypes

class SvelteJSParserDefinition : ECMA6ParserDefinition() {
    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createJSParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
        return SvelteJSParser(builder)
    }

    override fun createElement(node: ASTNode): PsiElement {
        return try {
            SvelteElementTypes.createElement(node)
        } catch (e: Exception) {
            super.createElement(node)
        }
    }

    companion object {
        val FILE: IFileElementType = JSFileElementType.create(SvelteJSLanguage.INSTANCE)
    }
}

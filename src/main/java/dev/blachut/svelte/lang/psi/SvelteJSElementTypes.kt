package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lang.javascript.types.JSParameterElementType
import dev.blachut.svelte.lang.SvelteJSLanguage

object SvelteJSElementTypes {
    val PARAMETER = object : JSParameterElementType("EMBEDDED_PARAMETER") {
        override fun construct(node: ASTNode) = SvelteJSParameter(node)

        override fun toString(): String {
            return "Svelte" + super.toString()
        }
    }

    val EMBEDDED_CONTENT_MODULE = object : JSEmbeddedContentElementType(SvelteJSLanguage.INSTANCE, "MOD_SVELTE_JS_") {
        override fun isModule() = true

        override fun toModule() = this
    }
}

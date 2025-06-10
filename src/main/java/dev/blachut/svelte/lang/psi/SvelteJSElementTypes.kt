package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSCompositeElementType
import com.intellij.lang.javascript.types.*
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage

object SvelteJSElementTypes {
  const val STUB_VERSION = 2

  val CONST_TAG_VARIABLE: JSVariableElementType = object : JSVariableElementType("CONST_TAG_VARIABLE") {
    override fun construct(node: ASTNode): PsiElement = SvelteJSConstTagVariable(node)

    override fun toString(): String = "Svelte$debugName"
  }

  val PARAMETER = object : JSParameterElementType("EMBEDDED_PARAMETER") {
    override fun construct(node: ASTNode) = SvelteJSParameter(node)

    override fun toString(): String = "Svelte$debugName"
  }

  val REFERENCE_EXPRESSION: IElementType = object : JSCompositeElementType("SVELTE_JS_REFERENCE_EXPRESSION"), JSExpressionElementType {
    override fun createCompositeNode() = SvelteJSReferenceExpression(this)

    override fun toString(): String = "Svelte" + super.toString()
  }

  val EMBEDDED_CONTENT_MODULE = object : JSEmbeddedContentElementType(SvelteJSLanguage.INSTANCE, "MOD_SVELTE_JS_") {
    override fun isModule() = true
    override fun toModule() = this

    override fun construct(node: ASTNode) = SvelteJSEmbeddedContentImpl(node)
  }

  val EMBEDDED_CONTENT_MODULE_TS = object : TypeScriptEmbeddedContentElementType(SvelteTypeScriptLanguage.INSTANCE, "MOD_SVELTE_TS_") {
    override fun isModule() = true
    override fun toModule() = this

    override fun construct(node: ASTNode) = SvelteJSEmbeddedContentImpl(node)
  }
}

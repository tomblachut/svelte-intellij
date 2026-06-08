package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSCompositeElementType
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.lang.javascript.types.TypeScriptEmbeddedContentElementType
import com.intellij.lang.javascript.types.TypeScriptVariableElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.CONST_TAG_VARIABLE
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.CONST_TAG_VARIABLE_TS

internal object SvelteJSElementTypes {
  const val STUB_VERSION = 4

  val CONST_TAG_VARIABLE: JSVariableElementType = object : JSVariableElementType("CONST_TAG_VARIABLE") {
    override fun construct(node: ASTNode): PsiElement = SvelteJSConstTagVariable(node)

    override fun toString(): String = "Svelte$debugName"
  }

  /**
   * TypeScript-aware variant of [CONST_TAG_VARIABLE] for `{@const ...}` declarations.
   * Supports TypeScript type annotations like `{@const foo: Type = expr}`.
   */
  val CONST_TAG_VARIABLE_TS: TypeScriptVariableElementType = object : TypeScriptVariableElementType("CONST_TAG_VARIABLE_TS") {
    override fun construct(node: ASTNode): PsiElement = SvelteTSConstTagVariable(node)

    override fun toString(): String = "Svelte$debugName"
  }

  val LET_TAG_VARIABLE: JSVariableElementType = object : JSVariableElementType("LET_TAG_VARIABLE") {
    override fun construct(node: ASTNode): PsiElement = SvelteJSLetTagVariable(node)

    override fun toString(): String = "Svelte$debugName"
  }

  val LET_TAG_VARIABLE_TS: TypeScriptVariableElementType = object : TypeScriptVariableElementType("LET_TAG_VARIABLE_TS") {
    override fun construct(node: ASTNode): PsiElement = SvelteTSLetTagVariable(node)

    override fun toString(): String = "Svelte$debugName"
  }

  /**
   * Returns the tag-variable element type for a declaration tag, by mutability and language mode.
   * @param langMode the detected language mode (from `<script lang="...">`)
   * @param isConst `true` for `{@const}` / `{const}`, `false` for the mutable `{let}`
   */
  fun getDeclarationTagVariable(langMode: SvelteLangMode, isConst: Boolean): IElementType {
    val ts = langMode == SvelteLangMode.HAS_TS
    return when {
      isConst && ts -> CONST_TAG_VARIABLE_TS
      isConst       -> CONST_TAG_VARIABLE
      ts            -> LET_TAG_VARIABLE_TS
      else          -> LET_TAG_VARIABLE
    }
  }

  val PARAMETER: JSParameterElementType = object : JSParameterElementType("EMBEDDED_PARAMETER") {
    override fun construct(node: ASTNode) = SvelteJSParameter(node)

    override fun toString(): String = "Svelte$debugName"
  }

  val REFERENCE_EXPRESSION: IElementType = object : JSCompositeElementType("SVELTE_JS_REFERENCE_EXPRESSION"), JSExpressionElementType {
    override fun createCompositeNode() = SvelteJSReferenceExpression(this)

    override fun toString(): String = "Svelte" + super.toString()
  }

  val EMBEDDED_CONTENT_MODULE: JSEmbeddedContentElementType = object : JSEmbeddedContentElementType(SvelteJSLanguage.INSTANCE, "MOD_SVELTE_JS_") {
    override fun isModule() = true
    override fun toModule() = this

    override fun construct(node: ASTNode) = SvelteJSEmbeddedContentImpl(node)
  }

  val EMBEDDED_CONTENT_MODULE_TS: TypeScriptEmbeddedContentElementType = object : TypeScriptEmbeddedContentElementType(SvelteTypeScriptLanguage.INSTANCE, "MOD_SVELTE_TS_") {
    override fun isModule() = true
    override fun toModule() = this

    override fun construct(node: ASTNode) = SvelteJSEmbeddedContentImpl(node)
  }

  /**
   * Element type for the type parameter list from the generics attribute.
   * Example: `<script lang="ts" generics="T extends { text: string }">`
   */
  @JvmField
  val SCRIPT_GENERICS_TYPE_PARAMETER_LIST: SvelteScriptGenericsTypeParameterListElementType =
    SvelteScriptGenericsTypeParameterListElementType()

  /**
   * Element type for the embedded content inside generics attribute.
   * Wraps the type parameter list to provide proper PSI structure.
   */
  @JvmField
  val GENERICS_EXPRESSION_CONTENT: SvelteGenericsExpressionContentElementType =
    SvelteGenericsExpressionContentElementType(
      "GENERICS_EXPRESSION_CONTENT",
      SvelteTypeScriptLanguage.INSTANCE
    )
}

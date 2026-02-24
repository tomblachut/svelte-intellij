package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes
import dev.blachut.svelte.lang.psi.isSingleDollarPrefixedName

class SvelteJSParser(
  builder: PsiBuilder,
) : ES6Parser(
  SvelteJSLanguage.INSTANCE,
  builder,
) {
  override val expressionParser: ES6ExpressionParser<*> =
    object : ES6ExpressionParser<SvelteJSParser>(this@SvelteJSParser) {
      override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
        if (this.builder.tokenType === JSTokenTypes.AS_KEYWORD) {
          return -1
        }

        return super.getCurrentBinarySignPriority(allowIn, advance)
      }
    }

  override val functionParser: ES6FunctionParser<*> =
    object : ES6FunctionParser<SvelteJSParser>(this@SvelteJSParser) {
      override val parameterType: IElementType
        get() = svelteParameterType(builder) ?: super.parameterType
    }

  override fun buildTokenElement(type: IElementType) {
    return super.buildTokenElement(svelteBuildTokenElementType(type, builder))
  }
}

val markupContextKey: Key<in Any> = Key.create("markupContextKey")
val blockContextKey: Key<in Any> = Key.create("blockContextKey")

/**
 * Context key to indicate we're in a block that uses 'as' for Svelte binding syntax.
 * This includes {#each}, {#await}, {:then}, {:catch} blocks.
 * When this is true, top-level 'as' should be treated as Svelte syntax, not TypeScript assertions.
 */
val blockWithAsBindingKey: Key<in Any> = Key.create("blockWithAsBindingKey")

internal fun svelteParameterType(builder: PsiBuilder): IElementType? =
  if (builder.getUserData(markupContextKey) == true) SvelteJSElementTypes.PARAMETER else null

internal fun svelteBuildTokenElementType(type: IElementType, builder: PsiBuilder): IElementType =
  if (type === JSElementTypes.REFERENCE_EXPRESSION && isSingleDollarPrefixedName(builder.tokenText!!)) {
    SvelteJSElementTypes.REFERENCE_EXPRESSION
  }
  else {
    type
  }

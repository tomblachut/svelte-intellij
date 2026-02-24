package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptExpressionParser
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptFunctionParser
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage

class SvelteTSParser(
  builder: PsiBuilder,
) : TypeScriptParser(
  SvelteTypeScriptLanguage.INSTANCE,
  builder,
) {
  private var parenDepth = 0

  override val expressionParser: TypeScriptExpressionParser =
    object : TypeScriptExpressionParser(this@SvelteTSParser) {
      override fun parseParenthesizedExpression() {
        parenDepth++
        try {
          super.parseParenthesizedExpression()
        }
        finally {
          parenDepth--
        }
      }

      override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
        // In blocks with 'as' binding ({#each}, {#await}), treat top-level 'as' as Svelte syntax.
        // Parenthesized 'as' still works as TS type assertion: {#each (items as Item[]) as item}
        if (builder.getUserData(blockWithAsBindingKey) == true &&
            builder.tokenType === JSTokenTypes.AS_KEYWORD &&
            parenDepth == 0) {
          return -1
        }

        return super.getCurrentBinarySignPriority(allowIn, advance)
      }
    }

  override val functionParser: TypeScriptFunctionParser =
    object : TypeScriptFunctionParser(this@SvelteTSParser) {
      override val parameterType: IElementType
        get() = svelteParameterType(builder) ?: super.parameterType
    }

  override fun buildTokenElement(type: IElementType) {
    return super.buildTokenElement(svelteBuildTokenElementType(type, builder))
  }
}

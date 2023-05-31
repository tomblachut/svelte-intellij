package dev.blachut.svelte.lang.parsing.ts

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteTypeScriptParser(builder: PsiBuilder) : TypeScriptParser(SvelteTypeScriptLanguage.INSTANCE, builder) {
  override fun buildTokenElement(type: IElementType) {
    // there are too many places that uses element type JSElementTypes.REFERENCE_EXPRESSION,
    // so use the new one only for the specific references
    return super.buildTokenElement(
      if (type === JSElementTypes.REFERENCE_EXPRESSION && SvelteJSReferenceExpression.isDollarPrefixedName(builder.tokenText!!)) {
        SvelteJSElementTypes.REFERENCE_EXPRESSION
      }
      else {
        type
      }
    )
  }
}

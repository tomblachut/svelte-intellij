package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.psi.tree.IElementType

class SvelteJSParser(builder: PsiBuilder) : ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>,
    ES6FunctionParser<*>, JSPsiTypeParser<*>>(builder) {
    init {
        myStatementParser = object : ES6StatementParser<SvelteJSParser>(this) {
            override fun getVariableElementType(): IElementType {
                // TODO Try to crate lazy element that splits variable and $ prefix
                return super.getVariableElementType()
            }
        }

        myExpressionParser = object : ES6ExpressionParser<SvelteJSParser>(this) {
            override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
                if (this.builder.tokenType === JSTokenTypes.AS_KEYWORD) {
                    return -1
                }

                return super.getCurrentBinarySignPriority(allowIn, advance)
            }
        }
    }
}

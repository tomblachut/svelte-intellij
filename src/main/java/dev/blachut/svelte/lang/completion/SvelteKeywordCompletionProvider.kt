package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import dev.blachut.svelte.lang.icons.SvelteIcons
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val symbolTokens = setOf(JSTokenTypes.SHARP, JSTokenTypes.COLON, JSTokenTypes.DIV, JSTokenTypes.AT)
    private val completions = listOf("#if", "#each", "#await", ":else", ":then", ":catch", "#key", "@html", "@debug")

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val expression =
            parameters.position.parents(false).find { it.elementType == SvelteJSLazyElementTypes.CONTENT_EXPRESSION }!!
        if (expression.firstChild.siblings().any { SvelteTokenTypes.KEYWORDS.contains(it.elementType) }) {
            return
        }

        val token = parameters.position.prevLeaf()
        val newResult = if (token != null && symbolTokens.contains(token.elementType)) {
            result.withPrefixMatcher(token.text + result.prefixMatcher.prefix)
        } else {
            result
        }

        for (completion in completions) {
            newResult.addElement(
                LookupElementBuilder.create(completion)
                    .withBoldness(true)
                    .withIcon(SvelteIcons.GRAY)
                    .withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP)
            )
        }
    }
}

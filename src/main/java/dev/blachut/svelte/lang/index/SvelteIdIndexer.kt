package dev.blachut.svelte.lang.index

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.id.IdIndexEntry
import com.intellij.psi.impl.cache.impl.id.LexingIdIndexer
import com.intellij.util.indexing.FileContent
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteIdIndexer : LexingIdIndexer {
    override fun map(inputData: FileContent): Map<IdIndexEntry, Int> {
        return BaseFilterLexerUtil.calcIdEntries(inputData) { consumer ->
            SvelteFilterLexer(
                consumer,
                SyntaxHighlighterFactory.getSyntaxHighlighter(
                    SvelteHTMLLanguage.INSTANCE, inputData.project, inputData.file
                ).highlightingLexer
            )
        }
    }

    override fun getVersion(): Int {
        return 1
    }
}

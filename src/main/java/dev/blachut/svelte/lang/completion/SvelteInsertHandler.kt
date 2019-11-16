package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import dev.blachut.svelte.lang.codeInsight.ComponentImporter
import dev.blachut.svelte.lang.codeInsight.ComponentLookupObject

object SvelteInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val lookupObject = item.`object` as ComponentLookupObject

        val componentName = item.lookupString

        ComponentImporter.insertComponentImport(context.editor, context.file, lookupObject.file, componentName, lookupObject.moduleInfo)
    }
}

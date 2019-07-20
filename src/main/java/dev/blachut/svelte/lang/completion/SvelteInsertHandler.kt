package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import dev.blachut.svelte.lang.codeInsight.ComponentImporter
import dev.blachut.svelte.lang.codeInsight.ComponentLookupObject

class SvelteInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val lookupObject = item.`object` as ComponentLookupObject

        val componentName = item.lookupString

        if (lookupObject.props != null) {
            replaceWithLiveTemplate(lookupObject.props, context, componentName)
        }

        ComponentImporter.insertComponentImport(context.editor, context.file, lookupObject.file, componentName)
    }

    private fun replaceWithLiveTemplate(props: List<String?>, context: InsertionContext, componentName: String) {
        if (props.isEmpty()) {
            return
        }
        context.setAddCompletionChar(false)
        val templateManager = TemplateManager.getInstance(context.project)
        val joinedProps = props.mapIndexed { index, prop -> "$prop={\$PROP$index\$}" }.joinToString(" ").trim()
        if (joinedProps.isEmpty()) {
            return
        }
        val text = "$componentName $joinedProps>\$END\$</$componentName>"
        val template = templateManager.createTemplate(componentName, "Svelte", text)
        props.forEachIndexed { index, _ ->
            template.addVariable("PROP$index", TextExpression(""), true)
        }
        context.document.deleteString(context.startOffset, context.tailOffset)
        templateManager.startTemplate(context.editor, template)
    }

    companion object {
        val INSTANCE = SvelteInsertHandler()
    }
}
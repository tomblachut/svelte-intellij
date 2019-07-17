package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

class SvelteInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val obj = item.`object` as Map<*, *>
        val componentFile = obj["file"] as VirtualFile

        val relativePath = FileUtil.getRelativePath(
                context.file.virtualFile.parent.path,
                componentFile.path,
                '/'
        )

        val componentName = item.lookupString

        if (obj["props"] != null) {
            replaceWithLiveTemplate(obj["props"] as List<String>, context, componentName)
        }

        val comma = JSCodeStyleSettings.getSemicolon(context.file)
        val importCode = "import $componentName from \"./$relativePath\"$comma"

        val jsElement = PsiTreeUtil.findChildOfType(context.file, JSEmbeddedContent::class.java)

        if (jsElement != null) {
            val existingImports = ES6ImportPsiUtil.getImportDeclarations(jsElement)
            // check if component has already been imported
            if (existingImports.any { it.importedBindings.any { binding -> binding.name == componentName } }) return
            val importStatement = JSChangeUtil.createStatementFromTextWithContext(importCode, jsElement)!!.psi
            if (existingImports.size == 0) {
                // findPlaceAndInsertES6Import is buggy when inserting the first import
                val newLine = PsiParserFacade.SERVICE.getInstance(context.project).createWhiteSpaceFromText("\n")
                jsElement.addBefore(newLine, jsElement.firstChild)
                jsElement.addAfter(importStatement, jsElement.firstChild)
            } else {
                ES6CreateImportUtil.findPlaceAndInsertES6Import(
                        jsElement,
                        importStatement,
                        componentName,
                        context.editor
                )
            }
            CodeStyleManager.getInstance(context.project).reformat(jsElement)
        } else {
            val scriptBlock = XmlElementFactory.getInstance(context.project)
                    .createHTMLTagFromText("<script>\n$importCode\n</script>\n\n")
            // check if there's an empty script tag and replace it
            // an empty script tag does not contain JSEmbeddedContent
            val scriptTag = this.findScriptTag(context.file)
            if (scriptTag != null) {
                scriptTag.replace(scriptBlock)
            } else {
                context.file.addBefore(scriptBlock, context.file.firstChild)
            }
            CodeStyleManager.getInstance(context.project).reformat(scriptBlock)
        }
    }

    private fun replaceWithLiveTemplate(props: List<String>, context: InsertionContext, componentName: String) {
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

    private fun findScriptTag(file: PsiFile): XmlTag? {
        val tags = PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java)
        return tags.find { it.name == "script" && PsiTreeUtil.findChildOfType(it, JSEmbeddedContent::class.java) == null }
    }

    companion object {
        val INSTANCE = SvelteInsertHandler()
    }
}
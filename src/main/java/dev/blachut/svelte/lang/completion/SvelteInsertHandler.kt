package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

class SvelteInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val componentFile = item.`object` as VirtualFile

        val relativePath = FileUtil.getRelativePath(
                context.file.virtualFile.parent.path,
                componentFile.path,
                File.separatorChar
        )

        val componentName = item.lookupString

        val comma = JSCodeStyleSettings.getSemicolon(context.file)
        val importCode = "import $componentName from \"./$relativePath\"$comma"

        val jsElement = PsiTreeUtil.findChildOfType(context.file, JSEmbeddedContent::class.java)

        if (jsElement != null) {
            val existingImports = ES6ImportPsiUtil.getImportDeclarations(jsElement)
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
            context.file.addBefore(scriptBlock, context.file.firstChild)
            CodeStyleManager.getInstance(context.project).reformat(scriptBlock)
        }
    }

    companion object {
        val INSTANCE = SvelteInsertHandler()
    }
}
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

class ComponentImporter {

    fun insertComponentImport(editor: Editor?, currentFile: PsiFile, componentFile: VirtualFile, componentName: String) {
        if (editor == null) {
            return
        }
        val project = currentFile.project
        val relativePath = FileUtil.getRelativePath(
                currentFile.virtualFile.parent.path,
                componentFile.path,
                '/'
        )
        val comma = JSCodeStyleSettings.getSemicolon(currentFile)
        val importCode = "import $componentName from \"./$relativePath\"$comma"

        val jsElement = PsiTreeUtil.findChildOfType(currentFile, JSEmbeddedContent::class.java)


        if (jsElement != null) {
            val existingImports = ES6ImportPsiUtil.getImportDeclarations(jsElement)
            // check if component has already been imported
            if (existingImports.any { it.importedBindings.any { binding -> binding.name == componentName } }) return
            val importStatement = JSChangeUtil.createStatementFromTextWithContext(importCode, jsElement)!!.psi
            if (existingImports.size == 0) {
                // findPlaceAndInsertES6Import is buggy when inserting the first import
                val newLine = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n")
                jsElement.addBefore(newLine, jsElement.firstChild)
                jsElement.addAfter(importStatement, jsElement.firstChild)
            } else {
                ES6CreateImportUtil.findPlaceAndInsertES6Import(
                        jsElement,
                        importStatement,
                        componentName,
                        editor
                )
            }
            CodeStyleManager.getInstance(project).reformat(jsElement)
        } else {
            val scriptBlock = XmlElementFactory.getInstance(project)
                    .createHTMLTagFromText("<script>\n$importCode\n</script>\n\n")
            // check if there's an empty script tag and replace it
            // an empty script tag does not contain JSEmbeddedContent
            val scriptTag = this.findScriptTag(currentFile)
            if (scriptTag != null) {
                scriptTag.replace(scriptBlock)
            } else {
                currentFile.addBefore(scriptBlock, currentFile.firstChild)
            }
            CodeStyleManager.getInstance(project).reformat(scriptBlock)
        }
    }

    private fun findScriptTag(file: PsiFile): XmlTag? {
        val tags = PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java)
        return tags.find { it.name == "script" && PsiTreeUtil.findChildOfType(it, JSEmbeddedContent::class.java) == null }
    }

}
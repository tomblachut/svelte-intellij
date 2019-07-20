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
import com.intellij.xml.util.HtmlUtil

object ComponentImporter {
    fun insertComponentImport(editor: Editor?, currentFile: PsiFile, componentFile: VirtualFile, componentName: String) {
        if (editor == null) return

        val project = currentFile.project
        val importCode = getImportText(currentFile, componentFile, componentName)

        val scriptTag = findScriptTag(currentFile)
        // An empty script tag does not contain JSEmbeddedContent
        val jsElement = PsiTreeUtil.findChildOfType(scriptTag, JSEmbeddedContent::class.java)

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
            // Check if there's an empty script tag and replace it
            if (scriptTag != null) {
                scriptTag.replace(scriptBlock)
            } else {
                currentFile.addBefore(scriptBlock, currentFile.firstChild)
            }
            CodeStyleManager.getInstance(project).reformat(scriptBlock)
        }
    }

    fun getImportText(currentFile: PsiFile, componentFile: VirtualFile, componentName: String): String {
        val relativePath = FileUtil.getRelativePath(
                currentFile.virtualFile.parent.path,
                componentFile.path,
                '/'
        ) ?: ""
        val prefix = if (relativePath.startsWith("../")) "" else "./"
        val comma = JSCodeStyleSettings.getSemicolon(currentFile)
        return "import $componentName from \"$prefix$relativePath\"$comma"
    }

    private fun findScriptTag(file: PsiFile): XmlTag? {
        return PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java).find { HtmlUtil.isScriptTag(it) }
    }
}
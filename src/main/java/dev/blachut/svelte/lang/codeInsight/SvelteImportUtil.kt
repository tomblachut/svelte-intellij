package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.codeStyle.CodeStyleManager
import com.jetbrains.rd.util.firstOrNull
import dev.blachut.svelte.lang.getRelativePath
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

object SvelteImportUtil {
    fun getImportText(currentFile: VirtualFile, componentFile: VirtualFile, componentName: String, quote: String, moduleInfo: JSModuleNameInfo?): String {
        if (moduleInfo != null && moduleInfo.resolvedFile.extension != "svelte") {
            return "import {$componentName} from $quote${moduleInfo.moduleName}$quote"
        }

        val relativePath = getRelativePath(currentFile, componentFile)
        val prefix = if (relativePath.startsWith("../")) "" else "./"
        val path = prefix + relativePath

        return "import $componentName from $quote$path$quote"
    }

    fun insertComponentImport(editor: Editor, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String, moduleInfo: JSModuleNameInfo?) {
        if (currentFile !is SvelteHtmlFile) return

        val project = currentFile.project

        val embeddedContent = prepareInstanceScriptContent(currentFile)

        val existingBindings = ES6ImportPsiUtil.getImportDeclarations(embeddedContent)
        // check if component has already been imported
        if (existingBindings.any { it.importedBindings.any { binding -> binding.name == componentName } }) return

        val semicolon = JSCodeStyleSettings.getSemicolon(currentFile)
        val quote = JSCodeStyleSettings.getQuote(currentFile)
        val importCode = getImportText(currentFile.virtualFile, componentVirtualFile, componentName, quote, moduleInfo) + semicolon
        val importStatement = JSChangeUtil.createStatementFromTextWithContext(importCode, embeddedContent)!!.psi
        if (existingBindings.size == 0) {
            // findPlaceAndInsertES6Import is buggy when inserting the first import
            val newLine = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n")
            embeddedContent.addBefore(newLine, embeddedContent.firstChild)
            embeddedContent.addAfter(importStatement, embeddedContent.firstChild)
            return
        }

        if (moduleInfo != null) {
            val moduleName = moduleInfo.moduleName
            val existingImports: ES6ImportPsiUtil.ES6ExistingImports = ES6ImportPsiUtil.getExistingImports(embeddedContent, moduleName)
            val specifier = existingImports.specifiers.firstOrNull()?.value
            val declaration = specifier?.declaration
            if (declaration != null) {
                val info = ES6ImportPsiUtil.CreateImportExportInfo(
                    null, componentName, ES6ImportPsiUtil.ImportExportType.SPECIFIER, ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT)
                ES6ImportPsiUtil.insertImportSpecifier(declaration, info)
                return
            }
        }

        ES6CreateImportUtil.findPlaceAndInsertES6Import(
            embeddedContent,
            importStatement,
            componentName,
            editor
        )

        CodeStyleManager.getInstance(project).reformat(embeddedContent)
    }
}

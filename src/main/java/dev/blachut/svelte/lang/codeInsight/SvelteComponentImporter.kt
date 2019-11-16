package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6FromClause
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathBuilder
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfigurationImpl
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.JSModuleNameInfoImpl
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.stubs.ES6ExportedNamesIndex
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.jetbrains.rd.util.firstOrNull
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteFile
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

object SvelteComponentImporter {
    fun insertComponentImport(editor: Editor, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String, moduleInfo: JSModuleNameInfo?) {
        if (currentFile !is SvelteHtmlFile) return

        val project = currentFile.project

        val jsElement = findOrCreateEmbeddedContent(project, currentFile) ?: return
        val existingBindings = ES6ImportPsiUtil.getImportDeclarations(jsElement)
        // check if component has already been imported
        if (existingBindings.any { it.importedBindings.any { binding -> binding.name == componentName } }) return

        val importCode = getImportText(currentFile, componentVirtualFile, componentName, moduleInfo)
        val importStatement = JSChangeUtil.createStatementFromTextWithContext(importCode, jsElement)!!.psi
        if (existingBindings.size == 0) {
            // findPlaceAndInsertES6Import is buggy when inserting the first import
            val newLine = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n")
            jsElement.addBefore(newLine, jsElement.firstChild)
            jsElement.addAfter(importStatement, jsElement.firstChild)
            return
        }

        if (moduleInfo != null) {
            val moduleName = moduleInfo.moduleName
            val existingImports: ES6ImportPsiUtil.ES6ExistingImports = ES6ImportPsiUtil.getExistingImports(jsElement, moduleName)
            val specifier = existingImports.specifiers.firstOrNull()?.value
            val declaration = specifier?.declaration
            if (declaration != null) {
                val info = ES6ImportPsiUtil.CreateImportExportInfo(null, componentName, ES6ImportPsiUtil.ImportExportType.SPECIFIER)
                ES6ImportPsiUtil.insertImportSpecifier(declaration, info)
                return
            }
        }

        ES6CreateImportUtil.findPlaceAndInsertES6Import(
            jsElement,
            importStatement,
            componentName,
            editor
        )

        CodeStyleManager.getInstance(project).reformat(jsElement)
    }

    fun getImportText(currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String, moduleInfo: JSModuleNameInfo?): String {
        val semicolon = JSCodeStyleSettings.getSemicolon(currentFile)
        val quote = JSCodeStyleSettings.getQuoteChar(currentFile)

        if (moduleInfo != null && moduleInfo.resolvedFile.extension != "svelte") {
            return "import {$componentName} from $quote${moduleInfo.moduleName}$quote$semicolon"
        }

        val relativePath = getRelativePath(currentFile, componentVirtualFile)
        val prefix = if (relativePath.startsWith("../")) "" else "./"
        val path = prefix + relativePath

        return "import $componentName from $quote$path$quote$semicolon"
    }

    fun getModulesInfos(project: Project, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String): MutableList<JSModuleNameInfo> {
        val infos = mutableListOf<JSModuleNameInfo>()
        val exports: Collection<JSElement> = StubIndex.getElements(ES6ExportedNamesIndex.KEY, componentName, project, GlobalSearchScope.allScope(project), JSElement::class.java)
        exports.forEach {
            if (it is ES6ExportSpecifier) {
                val declaration = it.declaration ?: return@forEach
                val from: ES6FromClause = PsiTreeUtil.findChildOfType(declaration, ES6FromClause::class.java)
                    ?: return@forEach
                val component = from.resolveReferencedElements().find { referencedFile ->
                    referencedFile is SvelteFile && referencedFile.viewProvider.virtualFile == componentVirtualFile
                }
                component ?: return@forEach
                val moduleVirtualFile = declaration.containingFile.virtualFile
                val configuration = JSImportPathConfigurationImpl(currentFile, it, moduleVirtualFile, false)
                infos.add(ES6CreateImportUtil.getExternalFileModuleName(JSImportPathBuilder.createBuilder(configuration)))
            }
        }
        infos.add(JSModuleNameInfoImpl(getRelativePath(currentFile, componentVirtualFile), componentVirtualFile, componentVirtualFile, currentFile, arrayOf("svelte"), true))
        return infos
    }

    private fun getRelativePath(currentFile: PsiFile, componentVirtualFile: VirtualFile): String {
        return FileUtil.getRelativePath(
            currentFile.virtualFile.parent.path,
            componentVirtualFile.path,
            '/'
        ) ?: ""
    }

    private fun findOrCreateEmbeddedContent(project: Project, currentFile: SvelteHtmlFile): JSEmbeddedContent? {
        var instanceScript = currentFile.instanceScript

        if (instanceScript == null) {
            val elementFactory = XmlElementFactory.getInstance(project)
            val emptyInstanceScript = elementFactory.createTagFromText("<script>\n</script>", SvelteHTMLLanguage.INSTANCE)
            val moduleScript = currentFile.moduleScript
            val document = currentFile.document!!

            instanceScript = if (moduleScript != null) {
                document.addAfter(emptyInstanceScript, moduleScript) as XmlTag
            } else {
                document.addBefore(emptyInstanceScript, document.firstChild) as XmlTag
            }
        }

        val jsElement = getJsEmbeddedContent(instanceScript)
        if (jsElement != null) return jsElement

        // instanceScript is empty, we need to insert something in order to parse JsEmbeddedContent
        val document = PsiDocumentManager.getInstance(project).getDocument(currentFile) ?: return null
        document.insertString(instanceScript.value.textRange.startOffset, "\n")
        PsiDocumentManager.getInstance(project).commitDocument(document)

        return getJsEmbeddedContent(instanceScript)
    }
}

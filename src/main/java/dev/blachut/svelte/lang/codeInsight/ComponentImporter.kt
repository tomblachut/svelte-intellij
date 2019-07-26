package dev.blachut.svelte.lang.codeInsight

import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6FromClause
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathBuilder
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfigurationImpl
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.stubs.ES6ExportedNamesIndex
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.usageView.UsageInfo
import com.intellij.util.ArrayUtil
import com.intellij.util.CommonProcessors
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.psi.SvelteFile
import java.util.*

object ComponentImporter {
    fun insertComponentImport(editor: Editor, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String) {
        val project = currentFile.project
        val importCode = getImportText(currentFile, componentVirtualFile, componentName)

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

    fun getImportText(currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String): String {
        val project = currentFile.project

        // if the component is exported by a module, import it from that module
        val importText = getFromReExports(project, currentFile, componentVirtualFile, componentName)
        if (importText != null) return importText

        val relativePath = FileUtil.getRelativePath(
            currentFile.virtualFile.parent.path,
            componentVirtualFile.path,
            '/'
        ) ?: ""
        val prefix = if (relativePath.startsWith("../")) "" else "./"
        val comma = JSCodeStyleSettings.getSemicolon(currentFile)
        return "import $componentName from \"$prefix$relativePath\"$comma"
    }

    private fun getFromReExports(project: Project, currentFile: PsiFile, componentVirtualFile: VirtualFile, componentName: String): String? {
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
                val moduleNameInfo = ES6CreateImportUtil.getExternalFileModuleName(JSImportPathBuilder.createBuilder(configuration))
                val moduleName = moduleNameInfo.moduleName
                return "import {$componentName} from \"$moduleName\""
            }
        }
        return null
    }

    private fun findScriptTag(file: PsiFile): XmlTag? {
        return PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java).find { HtmlUtil.isScriptTag(it) }
    }

    fun findUsages(targetElement: PsiElement, scope: SearchScope?): Collection<UsageInfo> {
        val project = targetElement.project
        val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement, false)

        val processor = CommonProcessors.CollectProcessor(Collections.synchronizedList(ArrayList<UsageInfo>()))
        val psiElements = ArrayUtil.mergeArrays(handler!!.primaryElements, handler.secondaryElements)
        val options = handler.getFindUsagesOptions(null)
        if (scope != null) options.searchScope = scope
        for (psiElement in psiElements) {
            handler.processElementUsages(psiElement, processor, options)
        }
        return processor.results
    }
}

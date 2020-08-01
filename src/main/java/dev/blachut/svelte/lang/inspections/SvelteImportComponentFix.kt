package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.SvelteImportUtil

// TODO Reuse ImportES6ModuleFix
class SvelteImportComponentFix(
    tag: XmlTag,
    private val quote: String,
    private val componentName: String,
    private val info: JSModuleNameInfo,
    private val currentFile: VirtualFile,
    private val componentFile: VirtualFile
) : LocalQuickFixOnPsiElement(tag) {
    override fun getText(): String {
        val wholeImportWrapQuote = ES6ImportPsiUtil.invertQuote(quote)
        val importText = SvelteImportUtil.getImportText(currentFile, componentFile, componentName, quote, info)

        return "Insert $wholeImportWrapQuote$importText$wholeImportWrapQuote"
    }

    override fun getFamilyName(): String {
        return "Insert import statement"
    }

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(startElement) ?: return
        SvelteImportUtil.insertComponentImport(editor, file, componentFile, componentName, info)
    }
}

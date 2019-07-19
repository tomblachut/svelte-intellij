package dev.blachut.svelte.lang.codeInsight


import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

class SvelteIntentionAction : PsiElementBaseIntentionAction() {

    override fun getText(): String {
        return "Import Svelte component"
    }

    override fun getFamilyName(): String {
        return "Import Svelte component"
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) {
            return false
        }

        if (element is XmlToken && StringUtil.isCapitalized(element.text)) {
            val parent = element.parent
            if (parent is XmlTag) {
                if (parent.descriptor?.declaration == null) {
                    val fileName = "${element.text}.svelte"
                    // check if we have a corresponding svelte file
                    val files = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project))
                    return files.isNotEmpty()
                }
            }
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val componentName = element.text
        val fileName = "$componentName.svelte"
        val files = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project))
        val file = files.firstOrNull()
        if (file != null) {
            ComponentImporter().insertComponentImport(editor, element.containingFile, file.virtualFile, componentName)
        }
    }

}
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class ComponentPropsProvider {

    fun getComponentProps(file: VirtualFile, project: Project): List<String?>? {
        val viewProvider = PsiManager.getInstance(project).findViewProvider(file) ?: return null
        val psiFile = viewProvider.getPsi(HTMLLanguage.INSTANCE) ?: return null

        val visitor = SvelteScriptVisitor()
        psiFile.accept(visitor)
        val jsElement = visitor.jsElement ?: return null

        val propsVisitor = PropsVisitor()
        jsElement.accept(propsVisitor)

        return propsVisitor.props
    }
}
package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.ComponentImporter
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return object : XmlElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {
                val componentName = tag.name
                if (!isSvelteComponentTag(componentName)) return
                if (tag.descriptor?.declaration != null) return

                val project = tag.project
                val file = tag.containingFile

                val fileName = "$componentName.svelte"
                // check if we have a corresponding svelte file
                val files = FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.allScope(project))
                if (files.isEmpty()) return

                files.forEach { virtualFile ->
                    val modulesInfos = ComponentImporter.getModulesInfos(project, file, virtualFile, componentName)
                    modulesInfos.forEach { info ->
                        val quickFix = object : LocalQuickFix {
                            override fun getFamilyName(): String {
                                return ComponentImporter.getImportText(file, virtualFile, componentName, info)
                            }

                            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                                val editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(tag) ?: return
                                ComponentImporter.insertComponentImport(editor, file, virtualFile, componentName, info)
                            }
                        }
                        holder.registerProblem(tag, displayName, quickFix)
                    }
                }
            }
        }
    }
}

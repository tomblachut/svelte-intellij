package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.ComponentImporter

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return object : XmlElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {

                val componentName = tag.name
                if (!StringUtil.isCapitalized(componentName)) return // TODO Extract isComponentTag function
                if (tag.descriptor?.declaration != null) return

                val fileName = "$componentName.svelte"
                // check if we have a corresponding svelte file
                val files = FilenameIndex.getVirtualFilesByName(tag.project, fileName, GlobalSearchScope.allScope(tag.project))
                if (files.isEmpty()) return

                files.forEach {
                    val quickFix = object : LocalQuickFix {
                        override fun getFamilyName(): String {
                            return ComponentImporter.getImportText(tag.containingFile, it, componentName)
                        }

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            val editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(tag)
                                ?: return
                            ComponentImporter.insertComponentImport(editor, tag.containingFile, it, componentName)
                        }
                    }

                    holder.registerProblem(tag, displayName, quickFix)
                }
            }
        }
    }
}
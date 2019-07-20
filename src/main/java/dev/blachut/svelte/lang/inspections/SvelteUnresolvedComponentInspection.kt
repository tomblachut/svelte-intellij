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
import com.intellij.psi.xml.XmlToken
import dev.blachut.svelte.lang.codeInsight.ComponentImporter

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {

    // a way to avoid duplicate inspections on the same tag
    val inspectedTags: MutableList<XmlTag> = mutableListOf()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        inspectedTags.clear()

        return object : XmlElementVisitor() {
            override fun visitXmlToken(token: XmlToken?) {
                if (token == null || token.parent !is XmlTag) return
                val tag = token.parent as XmlTag
                if (inspectedTags.contains(tag)) return

                val componentName = tag.name
                if (!StringUtil.isCapitalized(componentName)) return // TODO Extract isComponentTag function
                if (tag.descriptor?.declaration != null) return

                val fileName = "$componentName.svelte"
                // check if we have a corresponding svelte file
                val files = FilenameIndex.getFilesByName(tag.project, fileName, GlobalSearchScope.allScope(tag.project))
                if (files.isEmpty()) return

                files.forEach {
                    val quickFix = object : LocalQuickFix {
                        override fun getFamilyName(): String {
                            return ComponentImporter.getImportText(tag.containingFile, it.virtualFile, componentName)
                        }

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            val editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(tag)
                                ?: return
                            ComponentImporter.insertComponentImport(editor, tag.containingFile, it.virtualFile, componentName)
                        }
                    }

                    holder.registerProblem(tag, displayName, quickFix)
                }

                inspectedTags.add(tag)
            }
        }
    }
}

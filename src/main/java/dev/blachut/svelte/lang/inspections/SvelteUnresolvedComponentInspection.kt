package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.*
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.SvelteComponentImporter
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : XmlElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {
                if (!tag.isValid) return

                val componentName = tag.name
                if (!isSvelteComponentTag(componentName)) return
                if (tag.descriptor?.declaration != null) return

                val range = TextRange(1, tag.name.length + 1)

                val project = tag.project
                val fileName = "$componentName.svelte"
                // check if we have a corresponding svelte file
                val componentFiles = FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.allScope(project))
                if (componentFiles.isEmpty()) {
                    holder.registerProblem(tag, displayName, ProblemHighlightType.ERROR, range)
                    return
                }

                val currentFile = tag.containingFile

                componentFiles.forEach { virtualFile ->
                    val modulesInfos = SvelteComponentImporter.getModulesInfos(project, currentFile, virtualFile, componentName)
                    modulesInfos.forEach { info ->
                        // TODO Reuse ImportES6ModuleFix or LocalQuickFixOnPsiElement
                        val quickFix = object : LocalQuickFix {
                            override fun getName(): String {
                                val quoteString = JSCodeStyleSettings.getQuote(currentFile)
                                val wholeImportWrapQuote = ES6ImportPsiUtil.invertQuote(quoteString)
                                val importText = SvelteComponentImporter.getImportText(currentFile, virtualFile, componentName, info)

                                return "Insert $wholeImportWrapQuote$importText$wholeImportWrapQuote"
                            }

                            override fun getFamilyName(): String {
                                return "Insert import statement"
                            }

                            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                                val editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(tag) ?: return
                                SvelteComponentImporter.insertComponentImport(editor, currentFile, virtualFile, componentName, info)
                            }
                        }

                        holder.registerProblem(tag, displayName, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, range, quickFix)
                    }
                }
            }
        }
    }
}

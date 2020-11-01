package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.SvelteModuleUtil
import dev.blachut.svelte.lang.isSvelteComponentTag

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : XmlElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {
                if (!tag.isValid) return

                val componentName = tag.name
                if (!isSvelteComponentTag(componentName)) return
                if (tag.reference?.resolve() != null) return

                val range = TextRange(1, tag.name.length + 1)

                val project = tag.project
                val fileName = "$componentName.svelte"
                // check if we have a corresponding svelte file
                val componentFiles =
                    FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.allScope(project))
                if (componentFiles.isEmpty()) {
                    holder.registerProblem(tag, displayName, ProblemHighlightType.ERROR, range)
                    return
                }

                val currentFile = tag.containingFile
                val currentVirtualFile = currentFile.virtualFile
                val quote = JSCodeStyleSettings.getQuote(currentFile)

                componentFiles.forEach { componentVirtualFile ->
                    val moduleInfos =
                        SvelteModuleUtil.getModuleInfos(project, currentFile, componentVirtualFile, componentName)
                    moduleInfos.forEach { info ->
                        val quickFix = SvelteImportComponentFix(tag,
                            quote,
                            componentName,
                            info,
                            currentVirtualFile,
                            componentVirtualFile)
                        holder.registerProblem(tag,
                            displayName,
                            ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                            range,
                            quickFix)
                    }
                }
            }
        }
    }
}

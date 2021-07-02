package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.modules.JSImportModulesSuggester
import com.intellij.lang.javascript.modules.JSSimpleModuleReferenceInfo
import com.intellij.lang.javascript.modules.ModuleReferenceInfo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.ResolveResult
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
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

                val suggester = SvelteComponentImportModulesSuggester(JSSimpleModuleReferenceInfo(componentName), tag)
                val quickFixes = suggester.findFixes(ResolveResult.EMPTY_ARRAY)
                val message = suggester.getMessage(quickFixes) ?: displayName

                holder.registerProblem(
                    tag,
                    message,
                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                    range,
                    *quickFixes.toTypedArray(),
                )
            }
        }
    }
}

class SvelteComponentImportModulesSuggester(moduleReferenceInfo: ModuleReferenceInfo,
                                            node: PsiElement) : JSImportModulesSuggester(moduleReferenceInfo, node) {
    override fun isAvailableForES6Import(): Boolean = true
}

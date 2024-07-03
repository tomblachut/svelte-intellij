package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.modules.JSImportModulesSuggester
import com.intellij.lang.javascript.modules.JSSimpleModuleReferenceInfo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.SvelteHTMLLanguage
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

        val suggester = JSImportModulesSuggester(JSSimpleModuleReferenceInfo(componentName), tag)
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

  override fun isAvailableForFile(file: PsiFile): Boolean =
    file.language == SvelteHTMLLanguage.INSTANCE
}

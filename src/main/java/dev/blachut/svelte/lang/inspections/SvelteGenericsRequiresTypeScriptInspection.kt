// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.parsing.html.SvelteGenericsAttributeEmbeddedContentProvider.Companion.GENERICS_ATTRIBUTE_NAME
import org.jetbrains.annotations.Nls

/**
 * Inspection that validates that the `generics` attribute on script tags
 * requires TypeScript (`lang="ts"`).
 *
 * Example of invalid code:
 * ```svelte
 * <script generics="T extends string">
 *   // Error: generics requires TypeScript
 * </script>
 * ```
 *
 * Example of valid code:
 * ```svelte
 * <script lang="ts" generics="T extends string">
 *   // OK: TypeScript is enabled
 * </script>
 * ```
 */
class SvelteGenericsRequiresTypeScriptInspection : LocalInspectionTool() {
  override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Sentence) String =
    SvelteBundle.message("svelte.inspection.generics.requires.typescript.display.name")

  override fun getStaticDescription(): String =
    SvelteBundle.message("svelte.inspection.generics.requires.typescript.description")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        if (!tag.isValid) return

        // Only check script tags
        if (!HtmlUtil.isScriptTag(tag)) return

        // Check if generics attribute exists
        val genericsAttr = tag.getAttribute(GENERICS_ATTRIBUTE_NAME) ?: return

        // Check if lang="ts" is present
        val langAttr = tag.getAttribute(HtmlUtil.LANG_ATTRIBUTE_NAME)
        val langValue = langAttr?.value

        if (langValue != "ts") {
          // Report error on the generics attribute name
          val attrNameRange = getAttributeNameRange(genericsAttr)

          holder.registerProblem(
            genericsAttr,
            attrNameRange,
            SvelteBundle.message("svelte.inspection.generics.requires.typescript.message"),
            AddLangTypeScriptQuickFix()
          )
        }
      }
    }
  }

  override fun isAvailableForFile(file: PsiFile): Boolean =
    file.language == SvelteHTMLLanguage.INSTANCE

  /**
   * Gets the text range for the attribute name (not including the value).
   * This highlights just "generics" in `generics="T"`.
   */
  private fun getAttributeNameRange(attr: com.intellij.psi.xml.XmlAttribute): TextRange {
    val startOffset = attr.nameElement.startOffsetInParent
    val length = attr.nameElement.textLength
    return TextRange(startOffset, startOffset + length)
  }
}

/**
 * Quick fix that adds `lang="ts"` to the script tag.
 */
private class AddLangTypeScriptQuickFix : LocalQuickFix {
  override fun getFamilyName(): String =
    SvelteBundle.message("svelte.inspection.generics.requires.typescript.fix")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val genericsAttr = descriptor.psiElement as? com.intellij.psi.xml.XmlAttribute ?: return
    val tag = genericsAttr.parent as XmlTag

    // Add or update lang attribute
    tag.setAttribute(HtmlUtil.LANG_ATTRIBUTE_NAME, "ts")

    // Force reparse so the embedded script content is recognized as TypeScript
    // This is necessary because PSI modification alone doesn't trigger language change
    // for embedded content - the stub tree needs to be rebuilt
    val file = tag.containingFile
    val virtualFile = file.virtualFile
    if (virtualFile != null) {
      PsiDocumentManager.getInstance(project).reparseFiles(listOf(virtualFile), true)
    }
  }
}

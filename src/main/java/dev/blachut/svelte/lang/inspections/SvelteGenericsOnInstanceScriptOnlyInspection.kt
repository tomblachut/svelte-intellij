// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
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
 * Inspection that validates that the `generics` attribute is only used on
 * instance scripts, not module scripts (`context="module"`).
 *
 * Example of invalid code:
 * ```svelte
 * <script context="module" generics="T">
 *   // Error: generics not allowed on module scripts
 * </script>
 * ```
 *
 * Example of valid code:
 * ```svelte
 * <script lang="ts" generics="T">
 *   // OK: instance script with generics
 * </script>
 * ```
 */
class SvelteGenericsOnInstanceScriptOnlyInspection : LocalInspectionTool() {
  override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Sentence) String =
    SvelteBundle.message("svelte.inspection.generics.instance.script.only.display.name")

  override fun getStaticDescription(): String =
    SvelteBundle.message("svelte.inspection.generics.instance.script.only.description")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        if (!tag.isValid) return

        // Only check script tags
        if (!HtmlUtil.isScriptTag(tag)) return

        // Check if generics attribute exists
        val genericsAttr = tag.getAttribute(GENERICS_ATTRIBUTE_NAME) ?: return

        // Check if context="module" is present
        val contextAttr = tag.getAttribute("context")
        val contextValue = contextAttr?.value

        if (contextValue == "module") {
          // Report error on the generics attribute name
          val attrNameRange = getAttributeNameRange(genericsAttr)

          holder.registerProblem(
            genericsAttr,
            attrNameRange,
            SvelteBundle.message("svelte.inspection.generics.instance.script.only.message"),
            RemoveGenericsAttributeQuickFix()
          )
        }
      }
    }
  }

  override fun isAvailableForFile(file: PsiFile): Boolean =
    file.language == SvelteHTMLLanguage.INSTANCE

  private fun getAttributeNameRange(attr: com.intellij.psi.xml.XmlAttribute): TextRange {
    val startOffset = attr.nameElement.startOffsetInParent
    val length = attr.nameElement.textLength
    return TextRange(startOffset, startOffset + length)
  }
}

/**
 * Quick fix that removes the `generics` attribute from the script tag.
 */
private class RemoveGenericsAttributeQuickFix : LocalQuickFix {
  override fun getFamilyName(): String =
    SvelteBundle.message("svelte.inspection.generics.instance.script.only.fix")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val genericsAttr = descriptor.psiElement as? com.intellij.psi.xml.XmlAttribute ?: return
    val tag = genericsAttr.parent as XmlTag

    // Remove the generics attribute
    tag.setAttribute(GENERICS_ATTRIBUTE_NAME, null)
  }
}

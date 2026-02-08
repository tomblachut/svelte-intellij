// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.parsing.html.SvelteGenericsAttributeEmbeddedContentProvider.Companion.GENERICS_ATTRIBUTE_NAME
import org.jetbrains.annotations.Nls

/**
 * Inspection that validates that only one script tag with `generics` attribute
 * exists in a component.
 *
 * Example of invalid code:
 * ```svelte
 * <script lang="ts" generics="T">
 *   export let first: T;
 * </script>
 *
 * <script lang="ts" generics="U">  <!-- Error: second generics -->
 *   export let second: U;
 * </script>
 * ```
 *
 * Example of valid code:
 * ```svelte
 * <script lang="ts" generics="T, U">
 *   export let first: T;
 *   export let second: U;
 * </script>
 * ```
 */
class SvelteMultipleGenericsScriptsInspection : LocalInspectionTool() {
  override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Sentence) String =
    SvelteBundle.message("svelte.inspection.multiple.generics.scripts.display.name")

  override fun getStaticDescription(): String =
    SvelteBundle.message("svelte.inspection.multiple.generics.scripts.description")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : PsiElementVisitor() {
      override fun visitFile(file: PsiFile) {
        if (file.language != SvelteHTMLLanguage.INSTANCE) return

        // Find all script tags with generics attribute
        val scriptsWithGenerics = mutableListOf<XmlTag>()

        file.accept(object : XmlRecursiveElementVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)

            if (HtmlUtil.isScriptTag(tag) && tag.getAttribute(GENERICS_ATTRIBUTE_NAME) != null) {
              scriptsWithGenerics.add(tag)
            }
          }
        })

        // If more than one script has generics, report error on all but the first
        if (scriptsWithGenerics.size > 1) {
          for (i in 1 until scriptsWithGenerics.size) {
            val tag = scriptsWithGenerics[i]
            val genericsAttr = tag.getAttribute(GENERICS_ATTRIBUTE_NAME)!!

            val attrNameRange = getAttributeNameRange(genericsAttr)

            holder.registerProblem(
              genericsAttr,
              SvelteBundle.message("svelte.inspection.multiple.generics.scripts.message"),
              ProblemHighlightType.ERROR,
              attrNameRange
            )
          }
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

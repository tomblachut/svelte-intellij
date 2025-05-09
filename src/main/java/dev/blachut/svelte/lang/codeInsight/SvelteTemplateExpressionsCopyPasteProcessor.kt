// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.util.parents
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.codeInsight.SvelteTemplateExpressionsCopyPasteProcessor.SvelteTemplateExpressionsImportsTransferableData
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent
import kotlinx.coroutines.Deferred
import java.awt.datatransfer.DataFlavor

class SvelteTemplateExpressionsCopyPasteProcessor : ES6CopyPasteProcessorBase<SvelteTemplateExpressionsImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = SVELTE_TEMPLATE_EXPRESSIONS_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    if (file !is SvelteHtmlFile) return false
    val isTS = getJsEmbeddedContent(file.moduleScript ?: file.instanceScript)?.language == SvelteTypeScriptLanguage.INSTANCE

    return (isTS && settings.isUseTypeScriptAutoImport)
           || (!isTS && settings.isUseJavaScriptAutoImport)
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is SvelteHtmlFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, XmlDocument::class, withSelf = true)
      .let { it !is JSExecutionScope && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean {
    var result = false
    parent.accept(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSElement(node: JSElement) {
        if (node is JSEmbeddedContentImpl && textRange.intersects(node.textRange)) {
          result = true
          stopWalking()
        }
      }
    })
    return result || parent.parents(true).any { it is JSEmbeddedContentImpl }
  }

  override fun createTransferableData(importedElementsDeferred: Deferred<List<ImportedElement>>): SvelteTemplateExpressionsImportsTransferableData =
    SvelteTemplateExpressionsImportsTransferableData(importedElementsDeferred)

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    super.getExportScope(file, caret)
    ?: WriteAction.compute<PsiElement, Throwable> {
      prepareInstanceScriptContent(
        file as? SvelteHtmlFile ?: return@compute null)
    }

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: SvelteTemplateExpressionsImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    if (imports.isNotEmpty()) {
      ES6CreateImportUtil.addRequiredImports(destinationModule, SvelteJSLanguage.INSTANCE, imports)
    }
  }

  class SvelteTemplateExpressionsImportsTransferableData(importedElementsDeferred: Deferred<List<ImportedElement>>) : ES6ImportsTransferableDataBase(importedElementsDeferred) {
    override fun getFlavor(): DataFlavor {
      return SVELTE_TEMPLATE_EXPRESSIONS_IMPORTS_FLAVOR
    }
  }

}

private val SVELTE_TEMPLATE_EXPRESSIONS_IMPORTS_FLAVOR = DataFlavor(SvelteTemplateExpressionsImportsTransferableData::class.java, "svelte es6 imports")
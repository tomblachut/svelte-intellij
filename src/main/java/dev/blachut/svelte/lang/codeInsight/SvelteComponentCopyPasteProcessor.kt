// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.javascript.web.js.WebJSResolveUtil.disableIndexUpToDateCheckIn
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent
import java.awt.datatransfer.DataFlavor
import kotlin.Pair
import com.intellij.openapi.util.Pair as OpenApiPair

class SvelteComponentCopyPasteProcessor : ES6CopyPasteProcessorBase<SvelteComponentCopyPasteProcessor.SvelteComponentImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = SVELTE_COMPONENT_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    if (file !is SvelteHtmlFile) return false
    val isTS = getJsEmbeddedContent(file.moduleScript ?: file.instanceScript)?.language == SvelteTypeScriptLanguage.INSTANCE

    return isTS && settings.isUseTypeScriptAutoImport
           || (!isTS && settings.isUseJavaScriptAutoImport)
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is SvelteHtmlFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, XmlDocument::class, withSelf = true)
      .let { it !is JSExecutionScope && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean {
    var result = false
    parent.acceptChildren(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSElement(node: JSElement) {
        if (node is JSEmbeddedContentImpl && textRange.intersects(node.textRange)) {
          result = true
          stopWalking()
        }
      }
    })
    return result && isAcceptablePasteContext(parent)
  }

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    super.getExportScope(file, caret)
    ?: WriteAction.compute<PsiElement, Throwable> {
      prepareInstanceScriptContent(file as? SvelteHtmlFile ?: return@compute null)
    }

  override fun processTextRanges(textRanges: List<Pair<PsiElement, TextRange>>): Set<ImportedElement> {
    val textRangesOnly = textRanges.map { it.second }
    val result = mutableSetOf<ImportedElement>()

    textRanges.forEach { (parent, range) ->
      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      disableIndexUpToDateCheckIn(parent) {
        parent.accept(object : XmlRecursiveElementWalkingVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
              val name = tag.name
              if (isSvelteComponentTag(name)) {
                val source = JSStubBasedPsiTreeUtil.resolveLocally(name, tag, false)
                if (source is ES6ImportExportDeclarationPart) {
                  elements.add(OpenApiPair(name, source))
                }
              }
            }
          }
        })
      }
      result.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)), textRangesOnly))
    }
    return result
  }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): SvelteComponentImportsTransferableData =
    SvelteComponentImportsTransferableData(importedElements)

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: SvelteComponentImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<com.intellij.openapi.util.Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    WriteAction.run<RuntimeException> {
      ES6CreateImportUtil.addRequiredImports(destinationModule, pasteContextLanguage, imports)
    }
  }

  class SvelteComponentImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor =
      SVELTE_COMPONENT_IMPORTS_FLAVOR
  }

  companion object {
    private val SVELTE_COMPONENT_IMPORTS_FLAVOR = DataFlavor(SvelteComponentImportsTransferableData::class.java,
                                                             "svelte component imports")
  }
}

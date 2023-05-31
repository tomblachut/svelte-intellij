package dev.blachut.svelte.lang.format

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.impl.source.SourceTreeToPsiMap

class SvelteFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val psiFile = formattingContext.psiElement.containingFile
    val documentModel = FormattingDocumentModelImpl.createOn(psiFile)

    val astNode = SourceTreeToPsiMap.psiElementToTree(psiFile)
    val formattingPolicy = SvelteHtmlPolicy(formattingContext.codeStyleSettings, documentModel)
    val block = SvelteXmlBlock(astNode, null, null, formattingPolicy, null, null, false)

    return XmlFormattingModel(psiFile, block, documentModel)
  }
}

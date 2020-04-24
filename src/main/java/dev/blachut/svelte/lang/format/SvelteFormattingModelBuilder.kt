package dev.blachut.svelte.lang.format

import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.impl.source.SourceTreeToPsiMap

class SvelteFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        val psiFile = element.containingFile
        val documentModel = FormattingDocumentModelImpl.createOn(psiFile)

        val astNode = SourceTreeToPsiMap.psiElementToTree(psiFile)
        val formattingPolicy = HtmlPolicy(settings, documentModel)
        val block = SvelteXmlBlock(astNode, null, null, formattingPolicy, null, null, false)

        return XmlFormattingModel(psiFile, block, documentModel)
    }
}

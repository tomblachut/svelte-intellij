package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.emmet.EmmetParser
import com.intellij.codeInsight.template.emmet.XmlEmmetParser
import com.intellij.codeInsight.template.emmet.ZenCodingTemplate
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl
import com.intellij.codeInsight.template.emmet.generators.ZenCodingGenerator
import com.intellij.codeInsight.template.emmet.tokens.ZenCodingToken
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.diagnostic.AttachmentFactory
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import kotlin.math.min

class SvelteZenCodingGeneratorImpl : XmlZenCodingGeneratorImpl() {
    private val simpleKeys = setOf("if", "await")
    private val simpleTemplate = { key: String -> "{#$key \$EXPRESSION\$}\$END\${/$key}" }
    private val eachTemplate = "{#each \$EXPRESSION\$ as \$PARAMS\$}\$END\${/each}"
    private val infixUnaryKeys = setOf("elseif", "then", "catch")
    private val infixUnaryTemplate = { key: String -> "{:$key \$EXPRESSION\$}\$END\$" }
    private val elseTemplate = "{:else}\$END\$"

    override fun isMyLanguage(language: Language): Boolean {
        return SvelteHtmlContextType.isMyLanguage(language)
    }

    override fun isMyContext(context: PsiElement, wrapping: Boolean): Boolean {
        return context.containingFile.viewProvider is SvelteFileViewProvider && (wrapping || SvelteHtmlTextContextType.isInContext(context))
    }

    override fun createParser(tokens: MutableList<ZenCodingToken>?, callback: CustomTemplateCallback, generator: ZenCodingGenerator?, surroundWithTemplate: Boolean): EmmetParser {
        return XmlEmmetParser(tokens, SvelteZenCodingCustomTemplateCallback(callback), generator, surroundWithTemplate)
    }

    override fun createTemplateByKey(key: String, forceSingleTag: Boolean): TemplateImpl? {
        if (key in simpleKeys) {
            val template = TemplateImpl("", simpleTemplate(key), "")
            template.addVariable("EXPRESSION", "EXPRESSION", "", true)
            return template
        }

        if (key == "each") {
            val template = TemplateImpl("", eachTemplate, "")
            template.addVariable("EXPRESSION", "EXPRESSION", "items", true)
            template.addVariable("PARAMS", "PARAMS", "item", true)
            return template
        }


        if (key in infixUnaryKeys) {
            val correctedKey = if (key == "elseif") "else if" else key
            val template = TemplateImpl("", infixUnaryTemplate(correctedKey), "")
            template.addVariable("EXPRESSION", "EXPRESSION", "", true)
            template.isToReformat = true
            return template
        }

        if (key == "else") {
            val template = TemplateImpl("", elseTemplate, "")
            template.isToReformat = true
            return template
        }

        return super.createTemplateByKey(key, forceSingleTag)
    }

    // Adds JSTokenTypes.RBRACE to boundary tokens from XmlZenCodingGenerator
    override fun computeTemplateKey(callback: CustomTemplateCallback): String? {
        val editor = callback.editor
        val currentOffset = editor.caretModel.offset
        var startOffset = min(editor.document.getLineStartOffset(editor.document.getLineNumber(currentOffset)), currentOffset)
        val documentText = editor.document.charsSequence
        var prevVisibleLeaf: PsiElement? = callback.context
        while (prevVisibleLeaf != null) {
            val textRange = prevVisibleLeaf.textRange
            val endOffset = textRange.endOffset
            if (endOffset <= currentOffset) {
                if (endOffset <= startOffset) {
                    break
                }
                val prevType = prevVisibleLeaf.node.elementType
                if (prevType === XmlTokenType.XML_TAG_END || prevType === XmlTokenType.XML_EMPTY_ELEMENT_END || prevType === JSTokenTypes.RBRACE) {
                    startOffset = endOffset
                    break
                }
            }
            prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf)
        }
        if (startOffset < 0 || currentOffset > documentText.length || currentOffset < startOffset) {
            Logger.getInstance(javaClass)
                .error("Error while calculating emmet abbreviation. Offset: $currentOffset; Start: $startOffset",
                    AttachmentFactory.createAttachment(editor.document))
            return null
        }
        val key = computeKey(documentText.subSequence(startOffset, currentOffset))
        return if (!StringUtil.isEmpty(key) && ZenCodingTemplate.checkTemplateKey(key!!, callback, this)) key else null
    }
}

class SvelteZenCodingCustomTemplateCallback(callback: CustomTemplateCallback) : CustomTemplateCallback(callback.editor, callback.file.viewProvider.getPsi(SvelteHTMLLanguage.INSTANCE))

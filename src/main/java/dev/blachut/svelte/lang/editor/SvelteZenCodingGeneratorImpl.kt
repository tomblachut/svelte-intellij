package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.HtmlTextContextType
import com.intellij.codeInsight.template.emmet.EmmetParser
import com.intellij.codeInsight.template.emmet.XmlEmmetParser
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl
import com.intellij.codeInsight.template.emmet.generators.ZenCodingGenerator
import com.intellij.codeInsight.template.emmet.tokens.ZenCodingToken
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.SvelteLanguage

class SvelteZenCodingGeneratorImpl : XmlZenCodingGeneratorImpl() {
    private val simpleKeys = setOf("if", "await")
    private val simpleTemplate = { key: String -> "{#$key \$EXPRESSION\$}\$END\${/$key}" }
    private val eachTemplate = "{#each \$EXPRESSION\$ as \$PARAMS\$}\$END\${/each}"
    private val infixUnaryKeys = setOf("elseif", "then", "catch")
    private val infixUnaryTemplate = { key: String -> "{:$key \$EXPRESSION\$}\$END\$" }
    private val elseTemplate = "{:else}\$END\$"

    override fun isMyLanguage(language: Language?): Boolean {
        return language === SvelteLanguage.INSTANCE || super.isMyLanguage(language)
    }

    override fun isMyContext(context: PsiElement, wrapping: Boolean): Boolean {
        return context.containingFile.viewProvider is SvelteFileViewProvider && (wrapping || HtmlTextContextType.isInContext(context))
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
}

class SvelteZenCodingCustomTemplateCallback(callback: CustomTemplateCallback) : CustomTemplateCallback(callback.editor, callback.file.viewProvider.getPsi(HTMLLanguage.INSTANCE))

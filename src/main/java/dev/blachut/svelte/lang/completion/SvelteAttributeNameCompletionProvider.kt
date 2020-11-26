// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.icons.SvelteIcons
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

class SvelteAttributeNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val xmlTag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java, false)
        val attribute = parameters.position.parent as? SvelteHtmlAttribute
        if (xmlTag == null || attribute == null) return

        if (xmlTag.name == "script" && xmlTag.getAttribute("lang") == null) {
            result.addElement(createLookupElement("lang=\"ts\"", 100))
        }

        if (xmlTag.name == "script" && xmlTag.getAttribute("context") == null) {
            result.addElement(createLookupElement("context=\"module\"", 90))
        }

        // TODO refactor into proper descriptors
        if (xmlTag.name == "style" && xmlTag.getAttribute("global") == null) {
            result.addElement(createLookupElement("global"))
        }

        if (xmlTag.name == "style" && xmlTag.getAttribute("src") == null) {
            result.addElement(createLookupElement("src"))
        }

        val directiveType = attribute.directive?.directiveType

        if (directiveType == null) {
            for (prefix in SvelteDirectiveUtil.getPrefixCompletions(xmlTag.name)) {
                result.addElement(createDirectivePrefixLookupElement(prefix, parameters))
            }
            return
        }

        if (attribute.valueElement == null) {
            directiveType.shorthandCompletionFactory?.invoke(attribute, parameters, result)
        }
        directiveType.longhandCompletionFactory?.invoke(attribute, parameters, result)

        val prefix = result.prefixMatcher.prefix
        val lastSeparatorIndex = prefix.lastIndexOf(SvelteDirectiveUtil.MODIFIER_SEPARATOR)
        if (lastSeparatorIndex < 0) return

        val newResult = result.withPrefixMatcher(prefix.substring(lastSeparatorIndex + 1))
        for (modifier in directiveType.modifiers) {
            newResult.addElement(createLookupElement(modifier))
        }
    }

    private fun createLookupElement(text: String, priority: Int? = null): LookupElement {
        return LookupElementBuilder
            .create(text)
            .withIcon(SvelteIcons.GRAY)
            .let {
                if (priority != null) {
                    PrioritizedLookupElement.withPriority(it, priority.toDouble())
                } else {
                    it
                }
            }
    }

    private fun createDirectivePrefixLookupElement(text: String, parameters: CompletionParameters): LookupElement {
        return LookupElementBuilder
            .create(text)
            .withBoldness(true)
            .withIcon(SvelteIcons.GRAY)
            .withTypeText("Directive")
            .withInsertHandler { insertionContext, _ ->
                insertionContext.setLaterRunnable {
                    CodeCompletionHandlerBase(CompletionType.BASIC)
                        .invokeCompletion(parameters.originalFile.project, parameters.editor)
                }
            }
    }
}

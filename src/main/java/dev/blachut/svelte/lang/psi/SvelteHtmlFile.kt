package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.parsing.html.SvelteHTMLParserDefinition

fun getJsEmbeddedContent(script: PsiElement?): JSEmbeddedContent? {
    return PsiTreeUtil.getChildOfType(script, JSEmbeddedContent::class.java)
}

fun isModuleScript(tag: XmlTag?): Boolean {
    return tag != null && HtmlUtil.isScriptTag(tag) && tag.getAttributeValue("context") == "module"
}

fun findAncestorScript(place: PsiElement): XmlTag? {
    // TODO optimize for XmlTag, or only walk up from JSElements?
    val parentScript = PsiTreeUtil.findFirstContext(place, false) {
        it is XmlTag && HtmlUtil.isScriptTag(it)
    }
    return parentScript as XmlTag?
}

class SvelteHtmlFile(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, SvelteHTMLParserDefinition.FILE) {
    val moduleScript get() = document?.children?.find { it is XmlTag && HtmlUtil.isScriptTag(it) && it.getAttributeValue("context") == "module" } as XmlTag?

    // By convention instanceScript is placed after module script
    // so it makes sense to resolve last script in case of ambiguity from missing context attribute
    // ambiguous scripts should then be highlighted by appropriate inspection
    val instanceScript get() = document?.children?.findLast { it is XmlTag && HtmlUtil.isScriptTag(it) && it.getAttributeValue("context") == null } as XmlTag?

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        document ?: return true

        // TODO ScriptSupportUtil.processDeclarations caches found script tags
        val parentScript = findAncestorScript(place)
        if (isModuleScript(parentScript)) {
            // place is inside module script, nothing more to process
            return true
        } else if (parentScript != null) {
            // place is inside instance script, process module script if available
            return processScriptDeclarations(processor, state, lastParent, place, moduleScript)
        } else {
            // place is inside template expression, process instance and then module script if available
            return processScriptDeclarations(processor, state, lastParent, place, instanceScript) &&
                processScriptDeclarations(processor, state, lastParent, place, moduleScript)
        }
    }

    private fun processScriptDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement,
        script: PsiElement?
    ): Boolean {
        return getJsEmbeddedContent(script)
            ?.processDeclarations(processor, state, lastParent, place) ?: true
    }

    override fun toString(): String {
        return "SvelteHtmlFile: $name"
    }
}

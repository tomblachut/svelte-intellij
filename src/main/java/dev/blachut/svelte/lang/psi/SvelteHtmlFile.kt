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

class SvelteHtmlFile(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, SvelteHTMLParserDefinition.FILE) {
    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        // Script tag is not an ancestor of Svelte expressions so we need to redirect processing here
        val scriptTag = PsiTreeUtil.findChildrenOfType(document, XmlTag::class.java).find { HtmlUtil.isScriptTag(it) }
            ?: return true
        if (PsiTreeUtil.isAncestor(scriptTag, place, false)) {
            // place is inside script tag, so declarations were already processed before walking up here
            return true
        }

        // JSEmbeddedContent is nested twice, see SvelteJSScriptContentProvider
        val jsRoot = PsiTreeUtil.getChildOfType(scriptTag, JSEmbeddedContent::class.java)?.firstChild
            ?: return true
        return jsRoot.processDeclarations(processor, state, lastParent, place)
    }
}

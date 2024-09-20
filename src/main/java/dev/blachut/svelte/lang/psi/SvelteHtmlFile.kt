package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSModuleStatusOwner
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlFileElementType
import dev.blachut.svelte.lang.psi.blocks.SvelteSnippetBlock

fun getJsEmbeddedContent(script: PsiElement?): JSEmbeddedContent? {
  return PsiTreeUtil.getChildOfType(script, JSEmbeddedContent::class.java)
}

fun isModuleScript(tag: XmlTag?): Boolean {
  return tag != null && HtmlUtil.isScriptTag(tag) && hasModuleAttribute(tag)
}

fun hasModuleAttribute(tag: XmlTag) = tag.getAttribute("module") != null // Svelte 5
                                       || tag.getAttributeValue("context") == "module" // Svelte 3-4

fun findAncestorScript(place: PsiElement): XmlTag? {
  // TODO optimize for XmlTag, or only walk up from JSElements?
  val parentScript = PsiTreeUtil.findFirstContext(place, false) {
    it is XmlTag && HtmlUtil.isScriptTag(it)
  }
  return parentScript as XmlTag?
}

class SvelteHtmlFile(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, SvelteHtmlFileElementType.FILE), JSModuleStatusOwner {
  override fun getModuleStatus(): JSModuleStatusOwner.ModuleStatus = JSModuleStatusOwner.ModuleStatus.ES6

  val moduleScript
    get() = document?.children?.find {
      it is XmlTag && HtmlUtil.isScriptTag(it) && hasModuleAttribute(it)
    } as XmlTag?

  // By convention instanceScript is placed after module script
  // so it makes sense to resolve last script in case of ambiguity from missing context attribute
  // ambiguous scripts should then be highlighted by appropriate inspection
  val instanceScript
    get() = document?.children?.findLast {
      it is XmlTag && HtmlUtil.isScriptTag(it) && !hasModuleAttribute(it)
    } as XmlTag?

  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
    val document = document ?: return true

    // TODO ScriptSupportUtil.processDeclarations caches found script tags
    val parentScript = findAncestorScript(place)
    if (isModuleScript(parentScript)) {
      // place is inside module script, nothing more to process
      return true
    }
    else if (parentScript != null) {
      // place is inside instance script, and its declarations were already processed, process template declarations, then module script
      return processTopLevelTemplateDeclarations(processor, state, lastParent, place, document) &&
             processScriptDeclarations(processor, state, lastParent, place, moduleScript)
    }
    else {
      // place is inside template expression, process template declarations, then instance and then module script
      return processTopLevelTemplateDeclarations(processor, state, lastParent, place, document) &&
             processScriptDeclarations(processor, state, lastParent, place, instanceScript) &&
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
    return getJsEmbeddedContent(script)?.processDeclarations(processor, state, lastParent, place) ?: true
  }

  private fun processTopLevelTemplateDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement,
    document: XmlDocument,
  ): Boolean {
    for (element in document.children) {
      if (element is SvelteSnippetBlock) {
        element.processDeclarations(processor, state, lastParent, place)
      }
    }
    return true
  }

  override fun toString(): String {
    return "SvelteHtmlFile: $name"
  }
}

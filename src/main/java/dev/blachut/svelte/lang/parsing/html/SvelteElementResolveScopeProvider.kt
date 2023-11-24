package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptWithFileTypeUnionInnerProviderService
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.util.application
import dev.blachut.svelte.lang.SvelteHtmlFileType

class SvelteElementResolveScopeProvider : JSElementResolveScopeProvider {
  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    // Refer to https://github.com/tomblachut/svelte-intellij/issues/170
    if (!element.isValid) return null

    val psiFile = element.containingFile
    if (psiFile?.fileType !is SvelteHtmlFileType) return null
    if (psiFile !is XmlFile) return null
    // TODO what if one script is TS and other one is not
    val scriptTagContent = JSUtils.findScriptTagContent(psiFile)

    if (scriptTagContent != null && DialectDetector.isTypeScript(scriptTagContent)) {
      return application.service<TypeScriptWithFileTypeUnionInnerProviderService>().getProvider()
        .getResolveScope(psiFile.viewProvider.virtualFile, element.project)
    }
    return null
  }
}

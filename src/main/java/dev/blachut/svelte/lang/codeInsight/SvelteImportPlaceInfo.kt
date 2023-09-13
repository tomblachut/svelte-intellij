package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult

class SvelteImportPlaceInfo(place: PsiElement, results: Array<ResolveResult>) : JSImportPlaceInfo(place, results) {
  override fun getDialectForImporting(): DialectOptionHolder {
    return DialectOptionHolder.JS_WITHOUT_JSX // have to force JS as long as code expressions in markup are always JS
  }
}
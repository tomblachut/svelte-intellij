package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement

class SvelteImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getPathSettings(place: PsiElement, extensionWithDot: String, auto: Boolean): ExtensionSettings? {
    return if (auto && extensionWithDot == svelteExtension) ExtensionSettings.FORCE_EXTENSION else null
  }
}
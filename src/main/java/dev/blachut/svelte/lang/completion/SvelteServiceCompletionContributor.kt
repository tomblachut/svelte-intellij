package dev.blachut.svelte.lang.completion

import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor
import dev.blachut.svelte.lang.SvelteHTMLLanguage

/**
 * Required to rebind [TypeScriptLanguageServiceCompletionContributor] to [SvelteHTMLLanguage] contexts
 */
class SvelteServiceCompletionContributor : TypeScriptLanguageServiceCompletionContributor()
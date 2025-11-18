package dev.blachut.svelte.lang.completion

import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptServiceCompletionContributor
import dev.blachut.svelte.lang.SvelteHTMLLanguage

/**
 * Required to rebind [TypeScriptServiceCompletionContributor] to [SvelteHTMLLanguage] contexts
 */
private class SvelteServiceCompletionContributor : TypeScriptServiceCompletionContributor()
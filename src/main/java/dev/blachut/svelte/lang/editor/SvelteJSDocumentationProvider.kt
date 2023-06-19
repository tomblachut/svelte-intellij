package dev.blachut.svelte.lang.editor

import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider

/**
 * Required to bypass lack of TS inside Svelte markup.
 *
 * No SvelteTS equivalent, TypeScriptDocumentationProvider is used directly there.
 */
class SvelteJSDocumentationProvider : TypeScriptDocumentationProvider()
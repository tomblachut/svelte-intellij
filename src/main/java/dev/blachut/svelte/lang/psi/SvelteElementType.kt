package dev.blachut.svelte.lang.psi

import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteElementType(debugName: String) : IElementType(debugName, SvelteHTMLLanguage.INSTANCE)

package dev.blachut.svelte.lang.psi

import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage

class SvelteJSElementType(debugName: String) : IElementType(debugName, SvelteJSLanguage.INSTANCE)

package dev.blachut.svelte.lang.psi

import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import org.jetbrains.annotations.NonNls

class SvelteElementType(@NonNls debugName: String) : IElementType(debugName, SvelteHTMLLanguage.INSTANCE)



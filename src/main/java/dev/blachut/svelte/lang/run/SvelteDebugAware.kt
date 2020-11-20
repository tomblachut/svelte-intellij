package dev.blachut.svelte.lang.run

import com.intellij.javascript.debugger.JavaScriptDebugAwareBase
import dev.blachut.svelte.lang.SvelteHtmlFileType

class SvelteDebugAware: JavaScriptDebugAwareBase(SvelteHtmlFileType.INSTANCE)

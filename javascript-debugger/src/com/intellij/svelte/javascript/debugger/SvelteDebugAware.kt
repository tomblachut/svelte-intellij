package com.intellij.svelte.javascript.debugger

import com.intellij.javascript.debugger.JavaScriptDebugAwareBase
import dev.blachut.svelte.lang.SvelteHtmlFileType

class SvelteDebugAware : JavaScriptDebugAwareBase(SvelteHtmlFileType)

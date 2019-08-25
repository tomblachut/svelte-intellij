package dev.blachut.svelte.lang

import com.intellij.openapi.util.text.StringUtil

fun isSvelteComponentTag(name: String): Boolean {
    // TODO Support namespaced components
    return StringUtil.isCapitalized(name)
}

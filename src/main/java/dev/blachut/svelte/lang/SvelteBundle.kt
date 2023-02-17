package dev.blachut.svelte.lang

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val SVELTE_BUNDLE = "messages.SvelteBundle"

object SvelteBundle : DynamicBundle(SVELTE_BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = SVELTE_BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = SVELTE_BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}

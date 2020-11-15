package dev.blachut.svelte.lang.icons

import com.intellij.ui.IconManager
import javax.swing.Icon

object SvelteIcons {
    private fun load(path: String): Icon {
        return IconManager.getInstance().getIcon(path, SvelteIcons::class.java)
    }

    val COLOR = load("/icons/desaturated.svg")
    val GRAY = load("/icons/gray.svg")
}

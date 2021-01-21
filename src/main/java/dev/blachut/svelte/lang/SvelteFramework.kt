package dev.blachut.svelte.lang

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import dev.blachut.svelte.lang.icons.SvelteIcons
import javax.swing.Icon

class SvelteFramework : WebFramework() {
    override val displayName: String = "Svelte"
    override val icon: Icon = SvelteIcons.COLOR
    override val standaloneFileType: WebFrameworkHtmlFileType = SvelteHtmlFileType.INSTANCE

    companion object {
        val instance get() = get("svelte")!!
    }
}

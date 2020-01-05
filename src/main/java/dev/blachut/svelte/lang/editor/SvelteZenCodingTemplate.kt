// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.emmet.ZenCodingTemplate
import dev.blachut.svelte.lang.SvelteHTMLLanguage

// This is a hack around TemplateManagerImpl & multiple root PSI
class SvelteZenCodingTemplate : ZenCodingTemplate() {
    override fun isApplicable(callback: CustomTemplateCallback, offset: Int, wrapping: Boolean): Boolean {
        return SvelteHtmlContextType.isMyLanguage(callback.file.language)
    }

    override fun computeTemplateKey(callback: CustomTemplateCallback): String? {
        return super.computeTemplateKey(SvelteTemplateCallback(callback))
    }

    override fun expand(key: String, callback: CustomTemplateCallback) {
        super.expand(key, SvelteTemplateCallback(callback))
    }

    private class SvelteTemplateCallback(callback: CustomTemplateCallback)
        : CustomTemplateCallback(callback.editor, callback.file.viewProvider.getPsi(SvelteHTMLLanguage.INSTANCE))
}

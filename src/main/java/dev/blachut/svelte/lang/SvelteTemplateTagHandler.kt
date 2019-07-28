// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import dev.blachut.svelte.lang.html.SvelteHandledLexer

class SvelteTemplateTagHandler : BaseHtmlLexer.TokenHandler {
    companion object {
        const val SEEN_TEMPLATE: Int = 0x2000
    }

    override fun handleElement(lexer: Lexer?) {
        val handled = lexer as SvelteHandledLexer
        if (!handled.inTagState() && "template" == lexer.tokenText) {
            handled.setSeenTag(false)
        }
    }
}

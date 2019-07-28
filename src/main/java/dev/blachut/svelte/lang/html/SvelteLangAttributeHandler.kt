// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.html

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class SvelteLangAttributeHandler : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer) {
    val handled = lexer as SvelteHandledLexer
    val seenScript = handled.seenScript()
    val seenStyle = handled.seenStyle()
    if (!handled.seenTag() && !handled.inTagState()) {
      if (seenScript) {
        if ("lang" == lexer.tokenText) {
          handled.setSeenScriptType()
          handled.setSeenScript()
        }
      }
      else if (seenStyle) {
        if ("lang" == lexer.tokenText) {
          handled.setSeenStyleType()
        }
      }
    }
  }
}

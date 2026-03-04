package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.xml.HtmlLanguageStubVersionUtil
import dev.blachut.svelte.lang.SVELTE_LANG_MODE_KEY
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

// based on HtmlFileElementType
class SvelteHtmlFileElementType : IStubFileElementType<PsiFileStub<*>>("svelte file", SvelteHTMLLanguage.INSTANCE) {
  override fun getStubVersion(): Int {
    return HtmlLanguageStubVersionUtil.getHtmlStubVersion() + SvelteJSElementTypes.STUB_VERSION + 1 // +1 for lang mode support
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val project = psi.project
    val chars = chameleon.chars

    // PsiBuilderFactory.createBuilder fully lexes the file (calls lexer.start() + advances all tokens).
    // After this call, the lexer has detected the language mode by scanning <script lang="ts"> tags.
    val lexer = SvelteParsingLexer(SvelteHtmlLexer(false))
    val builder = PsiBuilderFactory.getInstance().createBuilder(
      project, chameleon, lexer, SvelteHTMLLanguage.INSTANCE, chars
    )

    val startTime = System.nanoTime()

    val langMode = lexer.lexedLangMode
    builder.putUserData(SVELTE_LANG_MODE_KEY, langMode)
    psi.putUserData(SVELTE_LANG_MODE_KEY, langMode)

    val parser = LanguageParserDefinitions.INSTANCE.forLanguage(SvelteHTMLLanguage.INSTANCE)!!.createParser(project)
    val node = parser.parse(this, builder)

    ParsingDiagnostics.registerParse(builder, language, System.nanoTime() - startTime)

    return node.firstChildNode
  }

  companion object {
    val FILE: SvelteHtmlFileElementType = SvelteHtmlFileElementType()
  }
}

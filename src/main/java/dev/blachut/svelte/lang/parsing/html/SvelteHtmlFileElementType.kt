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

// based on HtmlFileElementType and VueFileElementType
class SvelteHtmlFileElementType : IStubFileElementType<PsiFileStub<*>>("svelte file", SvelteHTMLLanguage.INSTANCE) {
  override fun getStubVersion(): Int {
    return HtmlLanguageStubVersionUtil.getHtmlStubVersion() + SvelteJSElementTypes.STUB_VERSION + 1 // +1 for lang mode support
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val project = psi.project
    val chars = chameleon.chars

    // Pre-lex the file to detect the language mode (looking for <script lang="ts">)
    // This is necessary because expressions need to know the lang mode before they're parsed,
    // but the mode is determined by scanning <script> tags which may appear anywhere in the file.
    val preLexer = SvelteParsingLexer(SvelteHtmlLexer(false))
    preLexer.start(chars, 0, chars.length, 0)
    while (preLexer.tokenType != null) {
      preLexer.advance()
    }
    val langMode = preLexer.lexedLangMode

    // Now create a fresh lexer for actual parsing
    val lexer = SvelteParsingLexer(SvelteHtmlLexer(false))
    val builder = PsiBuilderFactory.getInstance().createBuilder(
      project, chameleon, lexer, SvelteHTMLLanguage.INSTANCE, chars
    )

    val startTime = System.nanoTime()

    // Store the detected language mode for use during parsing
    builder.putUserData(SVELTE_LANG_MODE_KEY, langMode)
    psi.putUserData(SVELTE_LANG_MODE_KEY, langMode)

    // Parse using our builder that has the lang mode set
    val parser = LanguageParserDefinitions.INSTANCE.forLanguage(SvelteHTMLLanguage.INSTANCE)!!.createParser(project)
    val node = parser.parse(this, builder)

    ParsingDiagnostics.registerParse(builder, language, System.nanoTime() - startTime)

    return node.firstChildNode
  }

  companion object {
    val FILE = SvelteHtmlFileElementType()
  }
}
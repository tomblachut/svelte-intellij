package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.*
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage
import dev.blachut.svelte.lang.isTSLangValue
import dev.blachut.svelte.lang.psi.SvelteHtmlRawTextElementType
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlEmbeddedContentSupport : HtmlEmbeddedContentSupport {

  override fun isEnabled(lexer: BaseHtmlLexer): Boolean {
    return lexer is SvelteHtmlLexer
  }

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> =
    listOf(
      SvelteHtmlContentProvider(lexer),
      SvelteHtmlRawTextTagContentProvider(lexer),
      HtmlTokenEmbeddedContentProvider(
        lexer,
        SvelteTokenTypes.CODE_FRAGMENT,
        { JavaScriptHighlightingLexer(JSLanguageLevel.ES6.dialect.optionHolder) }
      )
    )

  class SvelteHtmlContentProvider(lexer: BaseHtmlLexer) : HtmlScriptStyleEmbeddedContentProvider(lexer) {

    override fun isInterestedInAttribute(attributeName: CharSequence): Boolean =
      namesEqual(attributeName, HtmlUtil.LANG_ATTRIBUTE_NAME) || super.isInterestedInAttribute(attributeName)

    override fun styleLanguage(styleLang: String?): Language? =
      CSSLanguage.INSTANCE.dialects.firstOrNull { it.id.equals(styleLang, ignoreCase = true) }
      ?: super.styleLanguage(styleLang)

    override fun scriptEmbedmentInfo(mimeType: String?): HtmlEmbedmentInfo =
      if (isTSLangValue(mimeType))
        HtmlLanguageEmbedmentInfo(SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE_TS, SvelteTypeScriptLanguage.INSTANCE)
      else
        HtmlLanguageEmbedmentInfo(SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE, SvelteJSLanguage.INSTANCE)
  }

  class SvelteHtmlRawTextTagContentProvider(lexer: BaseHtmlLexer) : HtmlRawTextTagContentProvider(lexer) {
    override fun createEmbedmentInfo(): HtmlEmbedmentInfo = SVELTE_RAW_TEXT_FORMATTABLE_EMBEDMENT
  }

}

val SVELTE_RAW_TEXT_FORMATTABLE_EMBEDMENT: HtmlEmbedmentInfo = object : HtmlEmbedmentInfo {
  override fun getElementType(): IElementType = SvelteHtmlRawTextElementType
  override fun createHighlightingLexer(): Lexer = SvelteHtmlRawTextLexer()
}

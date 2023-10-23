package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.xml.psi.XmlPsiBundle
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace
import dev.blachut.svelte.lang.psi.*

class SvelteHtmlParsing(builder: PsiBuilder) : HtmlParsing(builder) {

  private fun parseSvelteTag() {
    assert(token() === SvelteTokenTypes.START_MUSTACHE)
    val (tagToken, tagMarker) = SvelteTagParsing.parseTag(builder)
    val topLevelBlock = topLevelBlock
    if (SvelteTagElementTypes.START_TAGS.contains(tagToken)) {
      val openedBlock = SvelteBlockParsing.startBlock(tagToken, tagMarker, builder.mark())
      pushItemToStack(openedBlock)
    }
    else if (SvelteTagElementTypes.INNER_TAGS.contains(tagToken)) {
      if (topLevelBlock != null && topLevelBlock.isMatchingInnerTag(tagToken)) {
        flushOpenItemsWhile(tagMarker) { it !is SvelteBlock }
        topLevelBlock.handleInnerTag(tagToken, tagMarker, builder.mark())
      }
      else {
        tagMarker.precede().errorBefore(SvelteBundle.message("svelte.parsing.error.unexpected.inner.tag"), tagMarker)
      }
    }
    else if (SvelteTagElementTypes.END_TAGS.contains(tagToken)) {
      if (topLevelBlock != null && topLevelBlock.isMatchingEndTag(tagToken)) {
        flushOpenItemsWhile(tagMarker) { it !is SvelteBlock }
        (popItemFromStack() as SvelteBlock).handleEndTag(tagMarker)
      }
      else {
        tagMarker.precede().errorBefore(SvelteBundle.message("svelte.parsing.error.unexpected.end.tag"), tagMarker)
      }
    }
  }

  private val topLevelBlock: SvelteBlock?
    get() {
      var result: SvelteBlock? = null
      processStackItems {
        if (it is SvelteBlock) {
          result = it
          false
        }
        else true
      }
      return result
    }

  /**
   * Do not use in combination with [parseDocument]
   */
  internal fun parseRawTextContents() {
    var text: Marker? = null
    while (!builder.eof()) {
      if (builder.tokenType == SvelteTokenTypes.START_MUSTACHE) {
        text?.done(XmlElementType.XML_TEXT)
        text = null
        parseSvelteTag()
      }
      else {
        if (topLevelBlock != null && text == null) {
          text = builder.mark()
        }
        builder.advanceLexer()
      }
    }
    text?.done(XmlElementType.XML_TEXT)
    flushOpenItemsWhile { true }
  }

  override fun getHtmlTagElementType(info: HtmlTagInfo, tagLevel: Int): IElementType {
    return SvelteHtmlElementTypes.SVELTE_HTML_TAG
  }

  override fun isSingleTag(tagInfo: HtmlTagInfo): Boolean {
    // Inspired by Vue plugin. Svelte tags must be closed explicitly
    if (isSvelteComponentTag(tagInfo.originalName)) {
      return false
    }
    return super.isSingleTag(tagInfo)
  }

  override fun hasCustomTagContent(): Boolean {
    return token() === SvelteTokenTypes.START_MUSTACHE
  }

  override fun parseCustomTagContent(xmlText: Marker?): Marker? {
    terminateText(xmlText)
    parseSvelteTag()
    return null
  }

  override fun hasCustomTopLevelContent(): Boolean {
    return hasCustomTagContent()
  }

  override fun parseCustomTopLevelContent(error: Marker?): Marker? {
    flushError(error)
    parseSvelteTag()
    return null
  }

  override fun autoCloseItem(item: HtmlParserStackItem, beforeMarker: Marker?) {
    if (item is SvelteBlock)
      item.handleMissingEndTag(beforeMarker ?: builder.mark())
    else
      super.autoCloseItem(item, beforeMarker)
  }

  override fun hasCustomTagHeaderContent(): Boolean {
    return token() === SvelteTokenTypes.START_MUSTACHE
  }

  override fun parseCustomTagHeaderContent() {
    val att = mark()
    parseAttributeExpression(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)
    att.done(SvelteHtmlElementTypes.SVELTE_HTML_ATTRIBUTE)
  }

  override fun parseAttribute() {
    assert(token() === XmlTokenType.XML_NAME)
    val att = mark()

    val attributeName = builder.tokenText

    advance()
    if (token() === XmlTokenType.XML_EQ) {
      advance()
      parseAttributeValue(SvelteDirectiveUtil.chooseValueElementType(attributeName!!))
    }

    att.done(SvelteHtmlElementTypes.SVELTE_HTML_ATTRIBUTE)
  }

  private fun parseAttributeValue(elementType: IElementType) {
    val attValue = mark()
    if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      while (true) {
        val tt = token()
        if (tt == null ||
            tt === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER ||
            tt === XmlTokenType.XML_END_TAG_START ||
            tt === XmlTokenType.XML_EMPTY_ELEMENT_END ||
            tt === XmlTokenType.XML_START_TAG_START
        ) {
          break
        }

        if (tt === XmlTokenType.XML_BAD_CHARACTER) {
          val error = mark()
          advance()
          error.error(XmlPsiBundle.message("xml.parsing.unescaped.ampersand.or.nonterminated.character.entity.reference"))
        }
        else if (tt === XmlTokenType.XML_ENTITY_REF_TOKEN) {
          parseReference()
        }
        else if (tt === SvelteTokenTypes.START_MUSTACHE) {
          parseAttributeExpression(elementType)
        }
        else {
          advance()
        }
      }

      if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance()
      }
      else {
        error(XmlPsiBundle.message("xml.parsing.unclosed.attribute.value"))
      }
    }
    else {
      // Unquoted attr value. Unlike unmodified IntelliJ HTML this isn't necessary single token
      while (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN || token() === SvelteTokenTypes.START_MUSTACHE) {
        if (token() === SvelteTokenTypes.START_MUSTACHE) {
          parseAttributeExpression(elementType)
        }
        else {
          advance()
        }

        // Guard against adjacent shorthand or spread attributes ambiguity
        if (builder.isTokenAfterWhiteSpace()) {
          break
        }
      }
    }

    attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
  }

  private fun parseAttributeExpression(elementType: IElementType) {
    val expressionMarker = mark()
    advance() // {

    val marker = builder.mark()
    // Guard against empty expressions
    if (token() === SvelteTokenTypes.CODE_FRAGMENT) advance()
    marker.collapse(elementType)

    advance() // }
    expressionMarker.done(SvelteElementTypes.ATTRIBUTE_EXPRESSION)
  }

}

package dev.blachut.svelte.lang

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer

internal class SvelteHighlightingLexer(private val jsLanguageLevel: JSLanguageLevel) : HtmlHighlightingLexer()

//    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
//        val provider: HtmlScriptContentProvider?
//        //        if (mimeType != null) {
//        //            provider = super.findScriptContentProvider(mimeType) ?: scriptContentViaLang()
//        //        }
//        //        else {
//        provider = LanguageHtmlScriptContentProvider.getScriptContentProvider(jsLanguageLevel.dialect)
//        //        }
//        if (provider == null) {
//            return null
//        }
//
//        val moduleType = JSElementTypes.toModuleContentType(provider.scriptElementType)
//        return if (provider.scriptElementType === moduleType) provider else object : HtmlScriptContentProvider {
//            override fun getScriptElementType(): IElementType {
//                return moduleType
//            }
//
//            override fun getHighlightingLexer(): Lexer? {
//                return provider.highlightingLexer
//            }
//        }
//
//    }
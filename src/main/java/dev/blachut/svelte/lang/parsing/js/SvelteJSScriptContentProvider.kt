package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.ASTNode
import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.lexer.DummyLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.SvelteJSLanguage

object SvelteJSScriptContentProvider : HtmlScriptContentProvider {
    override fun getScriptElementType(): IElementType = EMBEDDED_CONTENT_WRAPPER

    override fun getHighlightingLexer(): Lexer? {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(SvelteJSLanguage.INSTANCE, null, null).highlightingLexer
    }

    fun getJsEmbeddedContent(script: PsiElement?): JSEmbeddedContent? {
        return PsiTreeUtil.getChildOfType(script, JSEmbeddedContent::class.java)?.firstChild as JSEmbeddedContent?
    }
}

/**
 * Svelte needs dedicated JS dialect to support $-features.
 * IntelliJ implements JS-inside-script-tag with JSEmbeddedContentElementType tokens.
 * There's two of them for every built-in JSLanguageDialect - one for classic top-level-is-global script, one for ES module.
 * Set of module tokens is hardcoded so you can't create token for a custom module dialect and expect code insight to work properly.
 * Many other classes use this hardcoded information to decide for e.g. how to highlight variables.
 *
 * Fortunately there is a pair of dialect tokens that can inherit dialect from parent JSEmbeddedContent.
 * SvelteJSLazyPsiElement is a JSEmbeddedContent that passes SvelteJSLanguage to JSStubElementTypes.EMBEDDED_CONTENT_MODULE
 * and in turn to its children.
 *
 * The only known side effect is that PSI is nested more than necessary.
 * Alternative is to implement JSFile which won't happen with MultiplePsiFilesPerDocumentFileViewProvider and current design
 *
 * @see com.intellij.lang.javascript.DialectDetector.calculateJSLanguage
 * @see com.intellij.lang.javascript.psi.impl.JSElementImpl.getLanguage
 * @see com.intellij.lang.javascript.psi.impl.JSStubElementImpl.getLanguage
 * @see com.intellij.lang.javascript.JSStubElementTypes.EMBEDDED_CONTENT_MODULE
 * @see com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.isEmbeddedModule
 */
private val EMBEDDED_CONTENT_WRAPPER = object : ILazyParseableElementType("EMBEDDED_CONTENT_WRAPPER", SvelteJSLanguage.INSTANCE) {
    override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
        val project = psi.project
        val lexer = DummyLexer(JSElementTypes.EMBEDDED_CONTENT_MODULE)
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, SvelteJSLanguage.INSTANCE, chameleon.chars)

        val rootMarker = builder.mark()
        builder.advanceLexer()
        rootMarker.done(this)

        return builder.treeBuilt.firstChildNode
    }

    override fun createNode(text: CharSequence?): ASTNode? {
        text ?: return null
        return SvelteJSScriptWrapperPsiElement(this, text)
    }
}

/**
 * @see com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
 */
class SvelteJSScriptWrapperPsiElement(type: IElementType, text: CharSequence) : LazyParseablePsiElement(type, text), JSTagEmbeddedContent {
    override fun accept(visitor: PsiElementVisitor) {
        when (visitor) {
            is JSElementVisitor -> visitor.visitJSEmbeddedContent(this)
            else -> super.accept(visitor)
        }
    }

    override fun toString(): String {
        return "SvelteJS: $elementType"
    }
}

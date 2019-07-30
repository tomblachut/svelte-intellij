package dev.blachut.svelte.lang.psi

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LazyParseableElement
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.util.CharTable
import dev.blachut.svelte.lang.SvelteLanguage
import org.jetbrains.annotations.NonNls

class SvelteCodeFragmentType(@NonNls debugName: String) : IElementType(debugName, SvelteLanguage.INSTANCE), EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase {

    override fun parse(text: CharSequence, table: CharTable): ASTNode {
        return LazyParseableElement(this, text)
    }

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val project: Project
        val psi = chameleon.psi
        project = psi.project

        val chars = chameleon.chars

        val lexer = createLexer(project)

        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chars)

        val level = JSRootConfiguration.getInstance(project).languageLevel
        val jsLanguageLevel = if (level.isES6Compatible) level else JSLanguageLevel.ES6

        builder.putUserData(JavaScriptParserBase.JS_DIALECT_KEY, jsLanguageLevel.dialect)

        val parser = jsLanguageLevel.dialect.createParser(builder)
        parser.parseJS(this)
        return builder.treeBuilt.firstChildNode
    }

    private fun createLexer(project: Project?): Lexer {
        return JSFlexAdapter(JSRootConfiguration.getInstance(project).languageLevel.dialect.optionHolder)
    }
}

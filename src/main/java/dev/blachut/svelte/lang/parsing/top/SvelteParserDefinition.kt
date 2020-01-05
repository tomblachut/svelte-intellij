package dev.blachut.svelte.lang.parsing.top

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageUtil
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.SvelteLanguage
import dev.blachut.svelte.lang.psi.*

class SvelteParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer {
//        val level = JSRootConfiguration.getInstance(project).languageLevel
//        return SvelteLexer(if (level.isES6Compatible) level else JSLanguageLevel.ES6)

        return SvelteLexer()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun createParser(project: Project): PsiParser {
        return SvelteParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return SvelteFile(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements {
        val lexer = this.createLexer(left.psi.project)
        return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer)
    }

    override fun createElement(node: ASTNode): PsiElement {
        if (node.elementType === SvelteJSElementTypes.ATTRIBUTE_EXPRESSION) {
            return ASTWrapperPsiElement(node)
        } else if (node.elementType === SvelteBlockLazyElementTypes.IF_END
            || node.elementType === SvelteBlockLazyElementTypes.EACH_END
            || node.elementType === SvelteBlockLazyElementTypes.AWAIT_END) {
            // TODO Create dedicated PsiElements for end tags
            return SveltePsiElementImpl(node)
        }

        return try {
            SvelteElementTypes.createElement(node)
        } catch (e: Exception) {
            SvelteTypes.Factory.createElement(node)
        }
    }

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        val FILE = IFileElementType(SvelteLanguage.INSTANCE)
    }
}

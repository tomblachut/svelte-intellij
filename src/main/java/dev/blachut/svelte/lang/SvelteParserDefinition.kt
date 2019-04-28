package dev.blachut.svelte.lang

import com.intellij.lang.ASTNode
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
import dev.blachut.svelte.lang.psi.SvelteFile
import dev.blachut.svelte.lang.psi.SvelteTypes

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
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        return SvelteTypes.Factory.createElement(node)
    }

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        val FILE = IFileElementType(SvelteLanguage.INSTANCE)
    }
}

//public class SvelteParserDefinition extends HTMLParserDefinition {
//    private static final IFileElementType HTML_FILE = new IStubFileElementType<PsiFileStub<HtmlFileImpl>>(SvelteLanguage.INSTANCE) {
//        @Override
//        public int getStubVersion() {
//            return super.getStubVersion() + JSFileElementType.getVersion();
//        }
//    };
//
//    @Override
//    @NotNull
//    public Lexer createLexer(Project project) {
//        JSLanguageLevel level = JSRootConfiguration.getInstance(project).getLanguageLevel();
//
//        return new SvelteLexer(level.isES6Compatible() ? level : JSLanguageLevel.ES6);
//    }
//
//    @Override
//    @NotNull
//    public PsiParser createParser(final Project project) {
//        return new HTMLParser();
//    }
//
//    @Override
//    public IFileElementType getFileNodeType() {
//        return HTML_FILE;
//    }
//
//    @Override
//    public PsiFile createFile(FileViewProvider viewProvider) {
//        return new HtmlFileImpl(viewProvider, HTML_FILE);
//    }
//}
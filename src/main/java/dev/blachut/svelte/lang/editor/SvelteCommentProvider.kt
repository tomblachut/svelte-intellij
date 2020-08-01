package dev.blachut.svelte.lang.editor

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.parsing.js.SvelteJSScriptWrapperPsiElement

/**
 * Overrides default behavior for Svelte tags and expressions
 */
class SvelteCommentProvider : MultipleLangCommentProvider {
    override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean {
        return isSvelteContext(file)
    }

    override fun getLineCommenter(
        file: PsiFile,
        editor: Editor,
        lineStartLanguage: Language?,
        lineEndLanguage: Language?
    ): Commenter? {
        if (lineStartLanguage?.isKindOf(JavascriptLanguage.INSTANCE) == true) {
            val offset = editor.caretModel.offset
            val wrapper =
                PsiTreeUtil.getContextOfType(file.findElementAt(offset), SvelteJSScriptWrapperPsiElement::class.java)

            val language = if (wrapper != null) {
                // inside script tag
                SvelteJSLanguage.INSTANCE
            } else {
                // inside template
                SvelteHTMLLanguage.INSTANCE
            }

            return LanguageCommenters.INSTANCE.forLanguage(language)
        }

        // Copied from CommentByBlockCommentHandler.getCommenter
        val fileLanguage = file.language
        val lang = if (
            lineStartLanguage == null ||
            LanguageCommenters.INSTANCE.forLanguage(lineStartLanguage) == null ||
            fileLanguage.baseLanguage === lineStartLanguage // file language is a more specific dialect of the line language
        ) fileLanguage else lineStartLanguage

        return LanguageCommenters.INSTANCE.forLanguage(lang)
    }
}

package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.SvelteLanguage

/**
 * Handler for custom plugin actions on chars typed by the user.  See [SvelteEnterHandler] for custom actions
 * on Enter.
 *
 * Based on Handlebars plugin
 */
class SvelteTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        val provider = file.viewProvider

        if (provider.baseLanguage != SvelteLanguage.INSTANCE) {
            return Result.CONTINUE
        }

        val offset = editor.caretModel.offset

        if (offset < 2 || offset > editor.document.textLength) {
            return Result.CONTINUE
        }

        val previousChar = editor.document.getText(TextRange(offset - 2, offset - 1))

        if (c == '}' && previousChar != "{") {
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            finishClosingTag(offset, editor, provider, true)
        } else if (c == '/' && previousChar == "{") {
            // TODO Remove this commit when "{#if variable}{" doesn't break parser
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            finishClosingTag(offset, editor, provider, false)
        }

        return Result.CONTINUE
    }

    private fun finishClosingTag(offset: Int, editor: Editor, provider: FileViewProvider, justAfterOpeningTag: Boolean) {

    }
}

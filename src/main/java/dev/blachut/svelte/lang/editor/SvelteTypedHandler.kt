package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock

/**
 * Handler for custom plugin actions on chars typed by the user.
 * See [SvelteEnterHandler] for custom actions on Enter.
 *
 * Based on Handlebars plugin
 */
class SvelteTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        val provider = file.viewProvider

        if (provider !is SvelteFileViewProvider) {
            return Result.CONTINUE
        }

        val offset = editor.caretModel.offset

        if (offset < 2 || offset > editor.document.textLength) {
            return Result.CONTINUE
        }

        val previousChar = editor.document.getText(TextRange(offset - 2, offset - 1))

        if (c == '}' && previousChar != "{") {
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            finishEndTag(offset, editor, provider, true)
        } else if (c == '/' && previousChar == "{") {
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            finishEndTag(offset, editor, provider, false)
        }

        return Result.CONTINUE
    }

    private fun finishEndTag(offset: Int, editor: Editor, provider: FileViewProvider, justAfterStartTag: Boolean) {
        val elementAtCaret = provider.findElementAt(offset - 1) ?: return
        val block = PsiTreeUtil.getParentOfType(elementAtCaret, SvelteBlock::class.java) ?: return

        if (block.endTag != null) return

        val prefix = if (justAfterStartTag) "{/" else ""

        val matchingTag = when (block.startTag.type) {
            SvelteTagElementTypes.IF_START -> prefix + "if}"
            SvelteTagElementTypes.EACH_START -> prefix + "each}"
            SvelteTagElementTypes.AWAIT_START -> prefix + "await}"
            else -> return
        }

        editor.document.insertString(offset, matchingTag)
        if (!justAfterStartTag) {
            editor.caretModel.moveToOffset(offset + matchingTag.length)
        }
    }
}

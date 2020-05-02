package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes

/**
 * Handler for custom plugin actions when `Enter` is typed by the user
 *
 * Based on Handlebars plugin
 */
class SvelteEnterHandler : EnterHandlerDelegateAdapter() {
    /**
     * if we are between start and end tags, we ensure the caret ends up in the "logical" place on Enter.
     * i.e. "{#if x}<caret>{/if}" becomes the following on Enter:
     *
     * {#if x}
     * <caret>
     * {/if}
     *
     * (Note: <caret> may be indented depending on formatter settings.)
     */
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (file.viewProvider is SvelteFileViewProvider && isBetweenSvelteTags(editor, file, caretOffset.get())) {
            originalHandler!!.execute(editor, editor.caretModel.currentCaret, dataContext)
            return EnterHandlerDelegate.Result.Default
        }
        return EnterHandlerDelegate.Result.Continue
    }

    /**
     * Checks to see if `Enter` has been typed while the caret is between an start and end tag pair
     */
    private fun isBetweenSvelteTags(editor: Editor, file: PsiFile, offset: Int): Boolean {
        if (offset == 0) return false
        val chars = editor.document.charsSequence
        if (chars[offset - 1] != '}') return false

        val highlighter = (editor as EditorEx).highlighter
        val iterator = highlighter.createIterator(offset - 1)
        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)

        val prevElement = file.findElementAt(iterator.start)
        PsiTreeUtil.findFirstParent(prevElement, true) { SvelteTagElementTypes.INITIAL_TAGS.contains(it.elementType) }
            ?: return false

        iterator.advance()
        if (iterator.atEnd()) {
            // no more tokens, so certainly no next tag
            return false
        }


        val nextElement = file.findElementAt(iterator.start)

        val tailTag = PsiTreeUtil.findFirstParent(nextElement, true) { SvelteTagElementTypes.TAIL_TAGS.contains(it.elementType) }
        // We're between matching tags if required tag is found
        return tailTag != null
    }
}

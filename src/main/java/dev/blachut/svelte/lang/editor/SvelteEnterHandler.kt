// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Copyright 2019 Tomasz Błachut
package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.psi.SvelteFile

/**
 * Handler for custom plugin actions when `Enter` is typed by the user
 *
 * Based on Handlebars plugin
 */
class SvelteEnterHandler : EnterHandlerDelegateAdapter() {
    /**
     * if we are between open and close tags, we ensure the caret ends up in the "logical" place on Enter.
     * i.e. "{#if x}<caret>{/if}" becomes the following on Enter:
     *
     * {#if x}
     * <caret>
     * {/if}
     *
     * (Note: <caret> may be indented depending on formatter settings.)
     */
    override fun preprocessEnter(file: PsiFile,
                                 editor: Editor,
                                 caretOffset: Ref<Int>,
                                 caretAdvance: Ref<Int>,
                                 dataContext: DataContext,
                                 originalHandler: EditorActionHandler?): EnterHandlerDelegate.Result {

        if (file is SvelteFile && isBetweenSvelteTags(editor, file, caretOffset.get())) {
            originalHandler!!.execute(editor, editor.caretModel.currentCaret, dataContext)
            return EnterHandlerDelegate.Result.Default
        }
        return EnterHandlerDelegate.Result.Continue
    }

    /**
     * Checks to see if `Enter` has been typed while the caret is between an open and close tag pair
     */
    private fun isBetweenSvelteTags(editor: Editor, file: PsiFile, offset: Int): Boolean {
        return false
    }
}

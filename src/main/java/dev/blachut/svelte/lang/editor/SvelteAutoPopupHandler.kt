// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.directives.SvelteDirectivesSupport
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

class SvelteAutoPopupHandler : TypedHandlerDelegate() {
    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (LookupManager.getActiveLookup(editor) != null) return Result.CONTINUE
        if (!isSvelteContext(file)) return Result.CONTINUE

        val element = file.findElementAt(editor.caretModel.offset)
        if (element?.parent !is SvelteHtmlTag) return Result.CONTINUE

        if (charTyped == SvelteDirectivesSupport.DIRECTIVE_SEPARATOR || charTyped == SvelteDirectivesSupport.MODIFIER_SEPARATOR) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }
}

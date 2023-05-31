// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil
import dev.blachut.svelte.lang.isScriptOrStyleTag
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.psi.SvelteHtmlElementTypes
import dev.blachut.svelte.lang.psi.SvelteHtmlTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

class SvelteAutoPopupHandler : TypedHandlerDelegate() {
  private val attributeExpectedChars =
    setOf(SvelteDirectiveUtil.DIRECTIVE_SEPARATOR, SvelteDirectiveUtil.MODIFIER_SEPARATOR)

  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (LookupManager.getActiveLookup(editor) != null) return Result.CONTINUE
    if (!isSvelteContext(file)) return Result.CONTINUE

    val offset = editor.caretModel.offset
    val element = file.findElementAt(offset)
                  ?: file.findElementAt(offset - 1)
                  ?: return Result.CONTINUE

    if (element.parent.elementType == SvelteHtmlElementTypes.SVELTE_HTML_TAG) {
      if (attributeExpectedChars.contains(charTyped)) {
        return handle(project, editor)
      }
    }

    val parentTag = element.parentOfType<SvelteHtmlTag>()

    if (charTyped == '{' && (parentTag == null || !parentTag.isScriptOrStyleTag())) {
      return handle(project, editor)
    }

    if (element.parent.elementType == SvelteJSLazyElementTypes.CONTENT_EXPRESSION) {
      if (charTyped == '#' || charTyped == '@') {
        return handle(project, editor)
      }
    }

    return Result.CONTINUE
  }

  private fun handle(project: Project, editor: Editor): Result {
    AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
    return Result.STOP
  }
}

package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.psi.AwaitStartType
import dev.blachut.svelte.lang.psi.EachStartType
import dev.blachut.svelte.lang.psi.IfStartType
import dev.blachut.svelte.lang.psi.KeyStartType
import dev.blachut.svelte.lang.psi.SnippetStartType
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock

/**
 * Inserts end tag when applicable
 */
class SvelteTagEndTypedHandler : TypedHandlerDelegate() {
  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (!isSvelteContext(file)) {
      return Result.CONTINUE
    }

    val offset = editor.caretModel.offset

    if (offset < 2 || offset > editor.document.textLength) {
      return Result.CONTINUE
    }

    val previousChar = editor.document.charsSequence[offset - 2]

    val beforeEndBrace = editor.document.textLength > offset && editor.document.charsSequence[offset] == '}'

    if (c == '}' && previousChar != '{') {
      PsiDocumentManager.getInstance(project).commitDocument(editor.document)
      finishEndTag(offset, editor, file, true, beforeEndBrace)
    }
    else if (c == '/' && previousChar == '{') {
      PsiDocumentManager.getInstance(project).commitDocument(editor.document)
      finishEndTag(offset, editor, file, false, beforeEndBrace)
    }

    return Result.CONTINUE
  }

  private fun finishEndTag(
    offset: Int,
    editor: Editor,
    file: PsiFile,
    justAfterStartTag: Boolean,
    beforeEndBrace: Boolean
  ) {
    val elementAtCaret = file.findElementAt(offset - 1) ?: return
    val block = PsiTreeUtil.getParentOfType(elementAtCaret, SvelteBlock::class.java) ?: return

    if (block.endTag != null) return

    val prefix = if (justAfterStartTag) "{/" else ""
    val suffix = if (beforeEndBrace) "" else "}"

    val matchingTag = when (block.startTag.type) {
      is IfStartType -> prefix + "if" + suffix
      is EachStartType -> prefix + "each" + suffix
      is AwaitStartType -> prefix + "await" + suffix
      is KeyStartType -> prefix + "key" + suffix
      is SnippetStartType -> prefix + "snippet" + suffix
      else -> return
    }

    editor.document.insertString(offset, matchingTag)
    if (!justAfterStartTag) {
      editor.caretModel.moveToOffset(offset + matchingTag.length + if (beforeEndBrace) 1 else 0)
    }
  }
}

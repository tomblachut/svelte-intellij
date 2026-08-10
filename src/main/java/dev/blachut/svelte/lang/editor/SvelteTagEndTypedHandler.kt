package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteContext
import dev.blachut.svelte.lang.psi.AwaitStartType
import dev.blachut.svelte.lang.psi.EachStartType
import dev.blachut.svelte.lang.psi.IfStartType
import dev.blachut.svelte.lang.psi.KeyStartType
import dev.blachut.svelte.lang.psi.SnippetStartType
import dev.blachut.svelte.lang.psi.blocks.SvelteBlock

/**
 * Typing assists around Svelte tags: inserts a block end tag when applicable, and keeps a `/` typed inside a
 * start tag header a plain `/`.
 *
 * The two behaviors share one handler deliberately. A [TypedHandlerDelegate] has to live in a module that
 * loads on both sides of a split IDE, and the Svelte plugin has no such module yet, so the plugin avoids
 * adding further implementations of it until it does.
 */
class SvelteTagEndTypedHandler : TypedHandlerDelegate() {
  /**
   * Keeps a `/` typed in the middle of an already closed start tag header a plain `/`, see WEB-77758.
   *
   * Svelte allows JS-style comments wherever an attribute name may start, so a `/` typed mid-header may
   * begin a comment. [com.intellij.codeInsight.editorActions.XmlSlashTypedHandler] misreads that position:
   * the freshly typed `/` breaks the header, the re-parsed tag loses its `>` and looks unclosed, and the
   * handler appends a second `>` into the middle of the tag. The decision therefore has to be taken BEFORE
   * the `/` is inserted, while the PSI is intact.
   *
   * Directly AT the tag end the platform assists stay as they always have in Svelte files: `/` before the
   * `>` of an empty `<div></div>` converts it to `<div/>`, `/` at an existing `/>` types over. So does the
   * `/` to `/>` auto-close of a genuinely unclosed tag and the `</` end tag completion. JSX sets no
   * precedent either way: its PSI is not XML, so the platform slash assists never fire there at all.
   */
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (c != '/' || !isSvelteContext(file)) return Result.CONTINUE

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    val offset = editor.caretModel.offset
    val element = file.viewProvider.findElementAt(offset, XMLLanguage::class.java) ?: return Result.CONTINUE
    // Attribute values and brace expressions are JS: a `/` is a division, a regex or a path there.
    if (element.parentOfType<XmlAttributeValue>(withSelf = true) != null) return Result.CONTINUE
    if (element.parentOfType<JSEmbeddedContent>(withSelf = true) != null) return Result.CONTINUE

    val node = (element.parentOfType<XmlTag>(withSelf = true) ?: return Result.CONTINUE).node
    val headerStart = startTagHeaderStart(node) ?: return Result.CONTINUE
    // No tag end: the tag is genuinely unclosed, let XmlSlashTypedHandler auto-close it.
    val tagEnd = startTagEndToken(node) ?: return Result.CONTINUE
    // Exclusive: exactly at the tag end the platform assists apply (empty-tag conversion, type-over).
    if (offset < headerStart || offset >= tagEnd.textRange.startOffset) return Result.CONTINUE

    EditorModificationUtil.insertStringAtCaret(editor, "/")
    return Result.STOP
  }

  /**
   * Inserts end tag when applicable
   */
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

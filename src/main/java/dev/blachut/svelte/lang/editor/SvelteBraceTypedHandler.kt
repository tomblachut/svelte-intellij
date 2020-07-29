package dev.blachut.svelte.lang.editor

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.TabOutScopesTracker
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.UndoManagerImpl
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorBundle
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import dev.blachut.svelte.lang.SvelteFileViewProvider
import dev.blachut.svelte.lang.psi.SvelteInitialTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyPsiElement
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

/**
 * Handler for custom plugin actions on chars typed by the user.
 * See [SvelteEnterHandler] for custom actions on Enter.
 */
class SvelteBraceTypedHandler : TypedHandlerDelegate() {
    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        if (c != '{' || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
            return Result.CONTINUE
        }

        if (file.viewProvider !is SvelteFileViewProvider) {
            return Result.CONTINUE
        }

        PsiDocumentManager.getInstance(project).commitDocument(editor.document)

        val offset = editor.caretModel.offset

        val element = file.findElementAt(offset) ?: return Result.CONTINUE

        if (element.elementType === SvelteTokenTypes.END_MUSTACHE || isNestedInSvelteElement(element)) {
            CommandProcessor.getInstance().currentCommandName = EditorBundle.message("typing.in.editor.command.name")
            EditorModificationUtil.insertStringAtCaret(editor, "{}", true, true, 1)
            (UndoManager.getInstance(project) as UndoManagerImpl).addDocumentAsAffected(editor.document)
            TabOutScopesTracker.getInstance().registerEmptyScope(editor, offset + 1)

            return Result.STOP
        }

        return Result.CONTINUE
    }
}

fun isNestedInSvelteElement(element: PsiElement): Boolean {
    val wrapper = PsiTreeUtil.getContextOfType(
        element,
        SvelteJSLazyPsiElement::class.java,
        SvelteInitialTag::class.java
    )
    return wrapper != null
}

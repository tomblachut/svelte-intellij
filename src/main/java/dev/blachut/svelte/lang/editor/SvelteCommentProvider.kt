package dev.blachut.svelte.lang.editor

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlAttributeValue
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.isSvelteContext

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
    lineStartLanguage: Language,
    lineEndLanguage: Language
  ): Commenter? {
    if (lineStartLanguage.isKindOf(JavascriptLanguage.INSTANCE)) {
      val startElement = file.findElementAt(editor.caretModel.offset)

      val jsMode = startElement?.contextOfType(
        JSTagEmbeddedContent::class, // inside script tag
        XmlAttributeValue::class,    // inside attribute value
      ) != null

      val language = if (jsMode) SvelteJSLanguage.INSTANCE else SvelteHTMLLanguage.INSTANCE
      return LanguageCommenters.INSTANCE.forLanguage(language)
    }

    // Copied from CommentByBlockCommentHandler.getCommenter
    val fileLanguage = file.language
    val lang = if (
      LanguageCommenters.INSTANCE.forLanguage(lineStartLanguage) == null ||
      fileLanguage.baseLanguage === lineStartLanguage // file language is a more specific dialect of the line language
    ) fileLanguage
    else lineStartLanguage

    return LanguageCommenters.INSTANCE.forLanguage(lang)
  }
}

package dev.blachut.svelte.lang.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.generation.IndentedCommenter
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
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.text.CharArrayUtil
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.isScriptOrStyleTag
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
    // `<!-- -->` is not valid inside a start tag header, Svelte expects JavaScript-style comments there.
    if (isInsideStartTagHeader(file, editor)) {
      val addsSpaceItself = CodeStyle.getLanguageSettings(file, lineStartLanguage).isLineCommentFollowedWithSpace
      return SvelteTagHeaderCommenter(addsSpaceItself)
    }

    if (lineStartLanguage.isKindOf(JavascriptLanguage)) {
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

  /**
   * Tells whether the line being commented starts inside a start tag header, i.e. in the place of an attribute.
   *
   * The extension point only receives the languages of the line, so the line itself has to be recovered from
   * the editor the same way [com.intellij.codeInsight.generation.CommentByLineCommentHandler] does it: the
   * first non-blank character of the line the action starts at.
   */
  private fun isInsideStartTagHeader(file: PsiFile, editor: Editor): Boolean {
    val caret = editor.caretModel.currentCaret
    val document = editor.document
    val actionOffset = if (caret.hasSelection()) caret.selectionStart else caret.offset
    val lineStart = document.getLineStartOffset(document.getLineNumber(actionOffset))
    val offset = CharArrayUtil.shiftForward(document.charsSequence, lineStart, " \t")

    // `canProcess` accepts Svelte files only, so this is the HTML tree of the file being edited.
    val element = file.findElementAt(offset) ?: return false
    // An attribute value is a language of its own, e.g. JavaScript in `on:click={() => {}}`.
    if (element.parentOfType<XmlAttributeValue>(withSelf = true) != null) return false

    val tag = element.parentOfType<XmlTag>(withSelf = true) ?: return false
    // The compiler reads the attributes of top-level <script>/<style> tags with read_static_attribute,
    // which accepts no comments, so a `//` in their headers would not compile. Nested ones are regular
    // elements and take comments like any other tag.
    if (tag.parentTag == null && tag.isScriptOrStyleTag()) return false

    val node = tag.node
    val headerStart = startTagHeaderStart(node) ?: return false
    val headerEnd = startTagEndToken(node)?.textRange?.startOffset
                    ?: node.textRange.endOffset // an unterminated tag, the header is all there is
    // Inclusive: on a line holding only the tag end, `// >` at least uses the right comment dialect,
    // while the HTML `<!-- -->` fallback would be invalid inside the header.
    return offset in headerStart..headerEnd
  }

  /**
   * Comments a start tag header the way the Svelte compiler reads it, see
   * [sveltejs/svelte#17671](https://github.com/sveltejs/svelte/pull/17671).
   *
   * Deliberately not the JavaScript commenter itself: the code style in effect is the HTML one, which puts
   * line comments in the first column and without a trailing space, so attributes would end up commented as
   * `//attribute={123}` glued to the left margin.
   *
   * @param platformAddsLineCommentSpace whether the caller appends the space after the prefix on its own
   */
  private class SvelteTagHeaderCommenter(platformAddsLineCommentSpace: Boolean) : IndentedCommenter {
    private val lineCommentPrefix = if (platformAddsLineCommentSpace) "//" else "// "

    override fun getLineCommentPrefix(): String = lineCommentPrefix

    override fun getBlockCommentPrefix(): String = "/*"

    override fun getBlockCommentSuffix(): String = "*/"

    // Same escaping as in JavaScript, nested block comments would end the outer one prematurely.
    override fun getCommentedBlockCommentPrefix(): String = "/!*"

    override fun getCommentedBlockCommentSuffix(): String = "*!/"

    override fun forceIndentedLineComment(): Boolean = true

    override fun forceIndentedBlockComment(): Boolean = true
  }
}

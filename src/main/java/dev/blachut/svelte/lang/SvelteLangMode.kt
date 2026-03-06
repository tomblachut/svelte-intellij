package dev.blachut.svelte.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerEx
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteLangModeMarkerElementType

/**
 * Determines the language mode for markup expressions in a Svelte file.
 *
 * If **any** `<script>` tag has `lang="ts"` or `lang="typescript"`, the entire file uses
 * TypeScript mode for all markup expressions. Within a single lexing pass, once TypeScript
 * mode is detected it applies to all subsequent expressions. The mode is re-evaluated
 * on each reparse, so adding or removing `lang="ts"` takes effect immediately.
 *
 * Example: `<script lang="ts">` makes `{#if (count as number) > 0}` parse as TypeScript.
 *
 * @see dev.blachut.svelte.lang.parsing.html.SvelteHtmlEmbeddedContentSupport
 * @see dev.blachut.svelte.lang.parsing.html.SvelteHtmlLexer.lexedLangMode
 * @see dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
 */
enum class SvelteLangMode(
  val exprLang: JSLanguageDialect,
  vararg val attrValues: String?
) {
  /** Initial state before lexing determines the mode */
  PENDING(SvelteJSLanguage.INSTANCE),

  /** No TypeScript detected - use JavaScript for expressions */
  NO_TS(SvelteJSLanguage.INSTANCE, "js", "javascript", null /* missing lang attr */),

  /** TypeScript detected - use TypeScript for expressions */
  HAS_TS(SvelteTypeScriptLanguage.INSTANCE, "ts", "typescript");

  /** Marker token emitted at end of lexing to encode the detected mode */
  internal val astMarkerToken: SvelteLangModeMarkerElementType =
    SvelteLangModeMarkerElementType(this)

  /** Canonical attribute value for serialization (e.g., "ts" or "js") */
  val canonicalAttrValue: String
    get() = if (this == HAS_TS) "ts" else "js"

  /**
   * Returns the element type name for the given base name.
   * For NO_TS (JavaScript), returns the base name as-is (e.g., "IF_START").
   * For HAS_TS (TypeScript), appends "_TS" suffix (e.g., "IF_START_TS").
   */
  fun toElementTypeName(baseName: String): String =
    if (this == HAS_TS) "${baseName}_TS" else baseName

  /**
   * Creates a highlighting lexer appropriate for this language mode.
   * Used for syntax highlighting of expressions in markup.
   */
  fun createExprHighlightingLexer(): Lexer =
    JavaScriptHighlightingLexer(exprLang.optionHolder)

  companion object {
    val DEFAULT: SvelteLangMode = NO_TS

    private val reverseMap: Map<String?, SvelteLangMode> =
      entries.flatMap { enumValue ->
        enumValue.attrValues.map { attrValue -> attrValue to enumValue }
      }.toMap()

    fun fromAttrValue(attrValue: String?): SvelteLangMode =
      reverseMap.getOrDefault(attrValue, NO_TS)

    /**
     * Gets the language mode for a file from the project and virtual file.
     * Used by syntax highlighter factory and other contexts where PSI may not be readily available.
     */
    fun getLatestKnownLang(project: Project?, virtualFile: VirtualFile?): SvelteLangMode {
      project ?: return DEFAULT
      virtualFile ?: return DEFAULT
      if (!virtualFile.isValid) return DEFAULT

      val file = if (virtualFile is VirtualFileWindow) {
        // InjectionRegistrarImpl.parseFile used in InjectionRegistrarImpl.reparse
        // doesn't set ViewProvider for some reason, and PsiManager#findFile fails because of that
        PsiManagerEx.getInstanceEx(project).fileManager.getCachedPsiFile(virtualFile)
      }
      else {
        PsiManager.getInstance(project).findFile(virtualFile)
      }
      return (file as? SvelteHtmlFile)?.langMode ?: DEFAULT
    }

    /**
     * Gets the language mode from a PSI element's containing file.
     */
    fun getLatestKnownLang(element: PsiElement): SvelteLangMode {
      return (element.containingFile as? SvelteHtmlFile)?.langMode ?: DEFAULT
    }
  }
}

/**
 * Key for storing the detected language mode in PsiBuilder user data.
 * Set during file parsing in [dev.blachut.svelte.lang.parsing.html.SvelteHtmlFileElementType].
 */
val SVELTE_LANG_MODE_KEY: Key<SvelteLangMode> = Key.create("svelte.lang.mode")

/**
 * Extension function to retrieve the Svelte language mode from a PsiBuilder.
 * Returns [SvelteLangMode.DEFAULT] if not set.
 */
fun PsiBuilder.getSvelteLangMode(): SvelteLangMode =
  getUserData(SVELTE_LANG_MODE_KEY) ?: SvelteLangMode.DEFAULT

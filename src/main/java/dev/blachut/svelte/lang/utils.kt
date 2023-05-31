package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.PsiBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.xml.util.HtmlUtil
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

fun isSvelteContext(context: PsiElement): Boolean {
  return context.containingFile is SvelteHtmlFile
}

fun isSvelteContext(file: VirtualFile): Boolean {
  return file.fileType == SvelteHtmlFileType.INSTANCE
}

fun isSvelteComponentTag(name: String): Boolean {
  // TODO Support namespaced components
  return StringUtil.isCapitalized(name)
}

fun isTSLangValue(value: String?): Boolean {
  return value == "ts" || value == "typescript"
}

fun PsiBuilder.isTokenAfterWhiteSpace(): Boolean {
  // tokenType is called because it skips whitespaces, unlike bare advanceLexer()
  this.tokenType
  val lastRawToken = this.rawLookup(-1)
  return lastRawToken === TokenType.WHITE_SPACE
}

fun SvelteHtmlTag.isScriptOrStyleTag(): Boolean {
  return this.name == HtmlUtil.SCRIPT_TAG_NAME || this.name == HtmlUtil.STYLE_TAG_NAME
}

internal inline fun <reified T : LocalInspectionTool> String.equalsName(): Boolean {
  return this == InspectionProfileEntry.getShortName(T::class.java.simpleName)
}

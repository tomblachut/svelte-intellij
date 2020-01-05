package dev.blachut.svelte.lang

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class SvelteSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        val level = if (project == null) {
            JSLanguageLevel.ES6
        } else {
            JSRootConfiguration.getInstance(project).languageLevel
        }

        return SvelteSyntaxHighlighter(level)
    }
}

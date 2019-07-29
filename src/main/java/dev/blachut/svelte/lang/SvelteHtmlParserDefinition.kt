package dev.blachut.svelte.lang

import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project

class SvelteHtmlParserDefinition : HTMLParserDefinition() {
    override fun createLexer(project: Project): Lexer {
        val level = JSRootConfiguration.getInstance(project).languageLevel
        return SvelteHtmlLexer(if (level.isES6Compatible) level else JSLanguageLevel.ES6)
    }
}

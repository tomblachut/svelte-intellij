package dev.blachut.svelte.lang

import com.intellij.lang.ecmascript6.ES6HandlersFactory
import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.codeInsight.SvelteAddImportExecutor

class SvelteHtmlHandlersFactory : ES6HandlersFactory() {
    override fun createAddImportExecutor(editor: Editor?, place: PsiElement): JSAddImportExecutor {
        return SvelteAddImportExecutor(editor, place)
    }
}

class SvelteJSHandlersFactory : ES6HandlersFactory() {
    override fun createAddImportExecutor(editor: Editor?, place: PsiElement): JSAddImportExecutor {
        return SvelteAddImportExecutor(editor, place)
    }
}

class SvelteTSHandlersFactory : TypeScriptHandlersFactory() {
    override fun createAddImportExecutor(editor: Editor?, place: PsiElement): JSAddImportExecutor {
        return SvelteAddImportExecutor(editor, place)
    }
}

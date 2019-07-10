package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

class SvelteInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val componentFile = item.`object` as VirtualFile

        val relativePath = FileUtil.getRelativePath(
                context.file.virtualFile.parent.path,
                componentFile.path,
                File.separatorChar
        )

        val componentName = item.lookupString
        val jsElement = PsiTreeUtil.findChildOfType(context.file, JSEmbeddedContent::class.java) as PsiElement
        val text = "import $componentName from \"./$relativePath\"" + JSCodeStyleSettings.getSemicolon(jsElement)
        val importStatement = JSChangeUtil.createStatementFromTextWithContext(text, jsElement)!!.psi

        ES6CreateImportUtil.findPlaceAndInsertES6Import(
                jsElement,
                importStatement,
                "import",
                context.editor)
    }

    companion object {
        val INSTANCE = SvelteInsertHandler()
    }
}
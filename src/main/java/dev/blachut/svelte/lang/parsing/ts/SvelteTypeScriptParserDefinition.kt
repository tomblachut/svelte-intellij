package dev.blachut.svelte.lang.parsing.ts

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.dialects.TypeScriptParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType
import dev.blachut.svelte.lang.SvelteTypeScriptLanguage

class SvelteTypeScriptParserDefinition : TypeScriptParserDefinition() {
    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createParser(project: Project?): PsiParser {
        return PsiParser { root, builder ->
            SvelteTypeScriptParser(builder).parseJS(root)
            builder.treeBuilt
        }
    }

    override fun createJSParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
        return SvelteTypeScriptParser(builder)
    }

    companion object {
        val FILE: IFileElementType = JSFileElementType.create(SvelteTypeScriptLanguage.INSTANCE)
    }
}

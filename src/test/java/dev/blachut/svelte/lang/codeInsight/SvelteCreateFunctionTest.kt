package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedFunctionInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection

class SvelteCreateFunctionTest : BaseSvelteCreateStatementTest(
    requestedAction = JavaScriptBundle.message("javascript.create.function.intention.name", "unresolved"),
    createdStatement = """
        function unresolved() {
            <trim>
        }
    """.replaceIndent(replacedIndent).replace("<trim>", ""),
    afterCaret = "()",
    optionalLine = "\n"
) {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(JSUnresolvedFunctionInspection(), TypeScriptUnresolvedFunctionInspection())
    }
}

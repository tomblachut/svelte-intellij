package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection

class SvelteCreateVariableTest : BaseSvelteCreateStatementTest(
    requestedAction = JavaScriptBundle.message("javascript.create.variable.intention.name", "unresolved"),
    createdStatement = """
        let unresolved;
    """.replaceIndent(replacedIndent),
    afterCaret = ""
) {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(JSUnresolvedReferenceInspection(),
                                    TypeScriptUnresolvedReferenceInspection())
    }
}

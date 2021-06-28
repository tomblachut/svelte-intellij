package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection

class SvelteCreateVariableTest : BaseSvelteCreateStatementTest(
    JavaScriptBundle.message("javascript.create.variable.intention.name", "unresolved"),
    """
        let unresolved;
    """.replaceIndent(replacedIndent),
    ""
) {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(JSUnresolvedVariableInspection(), TypeScriptUnresolvedVariableInspection())
    }
}

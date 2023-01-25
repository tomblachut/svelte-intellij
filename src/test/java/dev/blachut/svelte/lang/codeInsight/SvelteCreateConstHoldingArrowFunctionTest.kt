package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection

class SvelteCreateConstHoldingArrowFunctionTest : BaseSvelteCreateStatementTest(
    requestedAction = JavaScriptBundle.message("javascript.create.constant.holding.arrow.function.intention.name", "unresolved"),
    createdStatement = """
        const unresolved = () => {
            <trim>
        };
    """.replaceIndent(replacedIndent).replace("<trim>", ""),
    afterCaret = "()"
) {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(JSUnresolvedReferenceInspection(),
                                    TypeScriptUnresolvedReferenceInspection())
    }
}

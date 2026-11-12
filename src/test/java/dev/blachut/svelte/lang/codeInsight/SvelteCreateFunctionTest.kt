package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.BeforeEach

@TestApplication
class SvelteCreateFunctionTest : BaseSvelteCreateStatementTest(
    projectFixture = projectFixture,
    requestedAction = JavaScriptBundle.message("javascript.create.function.intention.name", "unresolved"),
    createdStatement = """
        function unresolved() {
            <trim>
        }
    """.replaceIndent(replacedIndent).replace("<trim>", ""),
    afterCaret = "()",
    optionalLine = "\n"
) {
    companion object {
        private val projectFixture = svelteProjectFixture()
    }

    @BeforeEach
    fun enableInspections() {
        myFixture.enableInspections(JSUnresolvedReferenceInspection(),
                                    TypeScriptUnresolvedReferenceInspection())
    }
}

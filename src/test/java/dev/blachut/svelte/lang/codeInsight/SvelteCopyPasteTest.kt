package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.pasteAndWaitJSCopyPasteService
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class SvelteCopyPasteTest {
    companion object {
        private val projectFixture = svelteProjectFixture()
    }

    private val myFixture by svelteCodeInsightFixture(projectFixture, getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/copyPaste")

    private lateinit var testName: String

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    }

    @Test
    fun testBasic() {
        doTest()
    }

    @Test
    fun testScriptToExpression() {
        doTest()
    }

    //region Test configuration and helper methods
    private fun doTest() {
        myFixture.copyDirectoryToProject(testName, ".")
        myFixture.configureFromTempProjectFile("Source.svelte")
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
        myFixture.configureFromTempProjectFile("Destination.svelte")
        myFixture.pasteAndWaitJSCopyPasteService()
        myFixture.checkResultByFile(testName + "/Destination_after.svelte")
    }
    //endregion

}
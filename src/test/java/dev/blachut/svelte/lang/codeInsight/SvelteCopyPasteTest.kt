package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteCopyPasteTest : BasePlatformTestCase() {

    fun testBasic() {
        doTest()
    }

    fun testScriptToExpression() {
        doTest()
    }

    //region Test configuration and helper methods
    override fun getTestDataPath(): String = getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/copyPaste"

    private fun doTest() {
        myFixture.copyDirectoryToProject(getTestName(true), ".")
        myFixture.configureFromTempProjectFile("Source.svelte")
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
        myFixture.configureFromTempProjectFile("Destination.svelte")
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
        myFixture.checkResultByFile(getTestName(true) + "/Destination_after.svelte")
    }
    //endregion

}
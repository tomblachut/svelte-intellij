package dev.blachut.svelte.lang.format

import com.intellij.psi.formatter.FormatterTestCase

class SvelteFormatterTest : FormatterTestCase() {
    override fun getTestDataPath(): String = "src/test/resources"
    override fun getBasePath(): String = "dev/blachut/svelte/lang/format"
    override fun getFileExtension(): String = "svelte"

    override fun getTestName(lowercaseFirstLetter: Boolean): String {
        // Yay OOP! Lol who has thought this can be readable
        return super.getTestName(false)
    }

    fun testNoSvelteBlocks() = doTest()
    fun testScriptContents() = doTest()
    fun testNestedBlocks() = doTest()
    fun testNestedBlocksFlat() = doTest()
    fun testIndentedExpressions() = doTest()
//    fun testOneLineBlock() = doTest() // TODO Fix formatter and enable test
}

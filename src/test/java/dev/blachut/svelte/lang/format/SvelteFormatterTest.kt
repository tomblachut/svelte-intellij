package dev.blachut.svelte.lang.format

import com.intellij.psi.formatter.FormatterTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteFormatterTest : FormatterTestCase() {
    override fun getTestDataPath(): String = getSvelteTestDataPath()
    override fun getBasePath(): String = "dev/blachut/svelte/lang/format"
    override fun getFileExtension(): String = "svelte"

    override fun getTestName(lowercaseFirstLetter: Boolean): String {
        return super.getTestName(false)
    }

    fun testNoSvelteBlocks() = doTest()
    fun testNestedBlocks() = doTest()
    fun testNestedBlocksFlat() = doTest()
    fun testIndentedExpressions() = doTest()
    fun testOneLineBlock() = doTest()

    fun testScriptContents() = doTest()
    fun testScriptContentsSingleLine() = doTest()
    fun testScriptStyleEmpty() = doTest()

    // WEB-77758: `//` and `/* */` comments inside start tags, see
    // https://github.com/sveltejs/svelte/pull/17671.
    // TagAttributes is the comment free baseline of TagComments: the two have to be indented alike.
    fun testTagAttributes() = doTest()
    fun testTagComments() = doTest()

    fun testMultilineExpression() = doTest()
    fun todoMultilineProp() = doTest()

  //fun testScriptGenericsFormat() = doTest()
  //fun testScriptGenericsMultiline() = doTest()
  //fun testScriptGenericsNested() = doTest()
  //fun testScriptGenericsUnionIntersection() = doTest()
}

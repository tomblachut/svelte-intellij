package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteEditorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = getSvelteTestDataPath()
    override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

    fun testEnterBetweenSvelteTags() {
        myFixture.configureByText("Foo.svelte", "{#if test}<caret>{/if}")
        myFixture.type('\n')
        myFixture.checkResult(
            """
                {#if test}
                    <caret>
                {/if}
                """.trimIndent()
        )
    }

    fun testEnterBetweenHtmlTags() {
        myFixture.configureByText("Foo.svelte", "<div><caret></div>")
        myFixture.type('\n')
        myFixture.checkResult(
            """
                <div>
                    <caret>
                </div>
                """.trimIndent()
        )
    }

    fun testCompleteSvelteTag() {
        myFixture.configureByText("Foo.svelte", "{#if test<caret>")
        myFixture.type('}')
        myFixture.checkResult("{#if test}<caret>{/if}")
    }

    fun testCompleteSvelteTagWithSlash() {
        myFixture.configureByText("Foo.svelte", "{#if test}hello{<caret>")
        myFixture.type('/')
        myFixture.checkResult("{#if test}hello{/if}<caret>")
    }

    fun testCompleteSvelteTag3() {
        myFixture.configureByText("Foo.svelte", """{#if true}{<caret>}""")
        myFixture.type("/")
        myFixture.checkResult("""{#if true}{/if}<caret>""")
    }

    fun testCompleteSvelteTagAcrossClauses() {
        myFixture.configureByText("Foo.svelte", """{#if true}{:else}{<caret>}""")
        myFixture.type("/")
        myFixture.checkResult("""{#if true}{:else}{/if}<caret>""")
    }

    fun testFoldingSvelteTag() {
        doTestFolding()
    }

    fun testFoldingEmptySvelteTag() {
        doTestFolding()
    }

    fun testFoldingSvelteTagInHtmlTag() {
        doTestFolding()
    }

    fun testFoldingEditorFold() {
        doTestFolding()
    }

    private fun doTestFolding() {
        myFixture.testFoldingWithCollapseStatus(testDataPath + "/" + basePath + "/" + getTestName(false) + ".svelte")
    }
}

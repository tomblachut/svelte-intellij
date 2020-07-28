package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteEditorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/resources"
    override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

    fun testEnterBetweenSvelteTags() {
        myFixture.configureByText("Foo.svelte", "{#if test}<caret>{/if}")
        myFixture.type('\n')
        myFixture.checkResult(
            """
                {#if test}
                    <caret>
                {/if}
                """.trimIndent())
    }

    fun testEnterBetweenHtmlTags() {
        myFixture.configureByText("Foo.svelte", "<div><caret></div>")
        myFixture.type('\n')
        myFixture.checkResult(
            """
                <div>
                    <caret>
                </div>
                """.trimIndent())
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

    fun testFoldingSvelteTag() {
        myFixture.testFoldingWithCollapseStatus(testDataPath + "/" + basePath + "/" +getTestName(false) + ".svelte")
    }
}

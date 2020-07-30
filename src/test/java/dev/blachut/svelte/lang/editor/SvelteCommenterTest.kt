// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

class SvelteCommenterTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources"
    override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

    fun testSvelteStartTag() {
        myFixture.initTest(
            """
            {#if true}<caret>
                test
            {/if}
            """.trimIndent()
        )
        myFixture.performEditorAction("CommentByLineComment")
        myFixture.checkResult(
            """
            <!--{#if true}-->
                test
            {/if}
            """.trimIndent()
        )
    }

    fun testSvelteEndTag() {
        myFixture.initTest(
            """
            {#if true}
                test
            {/if}<caret>
            """.trimIndent()
        )
        myFixture.performEditorAction("CommentByLineComment")
        myFixture.checkResult(
            """
            {#if true}
                test
            <!--{/if}-->
            """.trimIndent()
        )
    }

    private fun CodeInsightTestFixture.initTest(text: String) {
        this.configureByText("Foo.svelte", text)
    }
}



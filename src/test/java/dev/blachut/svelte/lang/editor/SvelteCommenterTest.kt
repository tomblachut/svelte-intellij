// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteCommenterTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()
  override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

  fun testSvelteStartTag() {
    myFixture.configureByText("Foo.svelte", """
      {#if true}<caret>
        test
      {/if}
    """.trimIndent())
    myFixture.performEditorAction("CommentByLineComment")
    myFixture.checkResult("""
      <!--{#if true}-->
        test
      {/if}
    """.trimIndent())
  }

  fun testSvelteEndTag() {
    myFixture.configureByText("Foo.svelte", """
      {#if true}
        test
      {/if}<caret>
    """.trimIndent())
    myFixture.performEditorAction("CommentByLineComment")
    myFixture.checkResult("""
      {#if true}
        test
      <!--{/if}-->
    """.trimIndent())
  }
}

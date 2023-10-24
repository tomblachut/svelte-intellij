// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.openapi.actionSystem.IdeActions.ACTION_COMMENT_BLOCK
import com.intellij.openapi.actionSystem.IdeActions.ACTION_COMMENT_LINE
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteCommenterTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()
  override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

  fun testScriptLineCommentForFirstLine() {
    doCommentLineTest(
      textBefore = """
      <script>
        test1()<caret>
        test2()
        test3()
      </script>
      """.trimIndent(),
      textAfter = """
      <script>
        // test1()
        test2()
        test3()
      </script>
      """.trimIndent(),
    )
  }

  fun testScriptLineCommentForFirstLine_ts() {
    doCommentLineTest(
      textBefore = """
      <script lang="ts">
        test1()<caret>
        test2()
        test3()
      </script>
      """.trimIndent(),
      textAfter = """
      <script lang="ts">
        // test1()
        test2()
        test3()
      </script>
      """.trimIndent(),
    )
  }

  fun testScriptLineCommentForLastLine() {
    doCommentLineTest(
      textBefore = """
      <script>
        test1()
        test2()
        test3()<caret>
      </script>
      """.trimIndent(),
      textAfter = """
      <script>
        test1()
        test2()
        // test3()
      </script>
      """.trimIndent(),
    )
  }

  fun testScriptLineCommentForAllLines() {
    doCommentLineTest(
      textBefore = """
      <script>
        <selection>test1()
        test2()
        test3()</selection><caret>
      </script>
      """.trimIndent(),
      textAfter = """
      <script>
        // test1()
        // test2()
        // test3()
      </script>
      """.trimIndent(),
    )
  }

  fun testScriptLineCommentForAllLines_ts() {
    doCommentLineTest(
      textBefore = """
      <script lang="ts">
        <selection>test1()
        test2()
        test3()</selection><caret>
      </script>
      """.trimIndent(),
      textAfter = """
      <script lang="ts">
        // test1()
        // test2()
        // test3()
      </script>
      """.trimIndent(),
    )
  }

  fun testInlineJsLineCommentForFirstLine() {
    doCommentLineTest(
      textBefore = """
      <button on:click={() => {
          action1()<caret>
          action2()
          action3()
      }}>
        Button label
      </button>
      """.trimIndent(),
      textAfter = """
      <button on:click={() => {
          // action1()
          action2()
          action3()
      }}>
        Button label
      </button>
      """.trimIndent(),
    )
  }

  fun testInlineJsLineCommentForLastLine() {
    doCommentLineTest(
      textBefore = """
      <button on:click={() => {
          action1()
          action2()
          action3()<caret>
      }}>
        Button label
      </button>
      """.trimIndent(),
      textAfter = """
      <button on:click={() => {
          action1()
          action2()
          // action3()
      }}>
        Button label
      </button>
      """.trimIndent(),
    )
  }

  fun testInlineJsLineCommentForAllLines() {
    doCommentLineTest(
      textBefore = """
      <button on:click={() => {
          <selection>action1()
          action2()
          action3()</selection><caret>
      }}>
        Button label
      </button>
      """.trimIndent(),
      textAfter = """
      <button on:click={() => {
          // action1()
          // action2()
          // action3()
      }}>
        Button label
      </button>
      """.trimIndent(),
    )
  }

  fun testInlineJsBlockCommentForWholeBlock() {
    doCommentBlockTest(
      textBefore = """
      <button on:click={<selection>() => {
          action1()
          action2()
          action3()
      }</selection><caret>}>
        Button label
      </button>
      """.trimIndent(),
      textAfter = """
      <button on:click={/*() => {
          action1()
          action2()
          action3()
      }*/}>
        Button label
      </button>
      """.trimIndent(),
    )
  }

  fun testInlineJsBlockCommentForAttributeValue() {
    doCommentBlockTest(
      textBefore = """
      <button on:click=<selection>{() => {
          action1()
          action2()
          action3()
      }}</selection><caret>>
        Button label
      </button>
      """.trimIndent(),
      textAfter = """
      <button on:click=<!--{() => {
          action1()
          action2()
          action3()
      }}-->>
        Button label
      </button>
      """.trimIndent(),
    )
  }

  fun testSvelteStartTag() {
    doCommentLineTest(
      textBefore = """
      {#if true}<caret>
        test
      {/if}
      """.trimIndent(),
      textAfter = """
      <!--{#if true}-->
        test
      {/if}
      """.trimIndent(),
    )
  }

  fun testSvelteEndTag() {
    doCommentLineTest(
      textBefore = """
      {#if true}
        test
      {/if}<caret>
      """.trimIndent(),
      textAfter = """
      {#if true}
        test
      <!--{/if}-->
      """.trimIndent(),
    )
  }

  private fun doCommentLineTest(
    textBefore: String,
    textAfter: String,
  ) {
    myFixture.configureByText("Foo.svelte", textBefore)
    myFixture.performEditorAction(ACTION_COMMENT_LINE)
    myFixture.checkResult(textAfter)
  }

  private fun doCommentBlockTest(
    textBefore: String,
    textAfter: String,
  ) {
    myFixture.configureByText("Foo.svelte", textBefore)
    myFixture.performEditorAction(ACTION_COMMENT_BLOCK)
    myFixture.checkResult(textAfter)
  }
}

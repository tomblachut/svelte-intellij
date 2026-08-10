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

  // WEB-77758: inside a start tag header a line comment must be `//`, not `<!-- -->`,
  // see https://github.com/sveltejs/svelte/pull/17671
  fun testStartTagLineCommentForAttribute() {
    doCommentLineTest(
      textBefore = """
      <MyComponent
          attribute={123}
          <caret>commentedAttribute={456}
          s="a string"
      />
      """.trimIndent(),
      textAfter = """
      <MyComponent
          attribute={123}
          // commentedAttribute={456}
          s="a string"
      />
      """.trimIndent(),
    )
  }

  fun testStartTagLineUncommentForAttribute() {
    doCommentLineTest(
      textBefore = """
      <MyComponent
          attribute={123}
          // <caret>commentedAttribute={456}
          s="a string"
      />
      """.trimIndent(),
      textAfter = """
      <MyComponent
          attribute={123}
          commentedAttribute={456}
          s="a string"
      />
      """.trimIndent(),
    )
  }

  fun testStartTagLineCommentForSeveralAttributes() {
    doCommentLineTest(
      textBefore = """
      <Component
          <selection>first={1}
          second={2}</selection><caret>
      />
      """.trimIndent(),
      textAfter = """
      <Component
          // first={1}
          // second={2}
      />
      """.trimIndent(),
    )
  }

  fun testStartTagLineUncommentForSeveralAttributes() {
    doCommentLineTest(
      textBefore = """
      <Component
          <selection>// first={1}
          // second={2}</selection><caret>
      />
      """.trimIndent(),
      textAfter = """
      <Component
          first={1}
          second={2}
      />
      """.trimIndent(),
    )
  }

  fun testStartTagLineCommentKeepsTagEndOnItsOwnLine() {
    doCommentLineTest(
      textBefore = """
      <div
          <caret>class="value"
      >
          Content
      </div>
      """.trimIndent(),
      textAfter = """
      <div
          // class="value"
      >
          Content
      </div>
      """.trimIndent(),
    )
  }

  fun testStartTagBlockCommentForAttribute() {
    doCommentBlockTest(
      textBefore = """
      <Component
          <selection>attribute={123}</selection><caret>
          s="a string"
      />
      """.trimIndent(),
      textAfter = """
      <Component
          /*attribute={123}*/
          s="a string"
      />
      """.trimIndent(),
    )
  }

  fun testTagContentLineCommentIsStillHtmlComment() {
    doCommentLineTest(
      textBefore = """
      <div
          class="value"
      >
      Content<caret>
      </div>
      """.trimIndent(),
      textAfter = """
      <div
          class="value"
      >
      <!--Content-->
      </div>
      """.trimIndent(),
    )
  }

  // On a line holding only the tag end, `// >` at least uses the right comment dialect for the header,
  // while the HTML `<!-- -->` fallback would be invalid there.
  fun testStartTagLineCommentOnTagEndLine() {
    doCommentLineTest(
      textBefore = """
      <div
          class="value"
      <caret>>
          Content
      </div>
      """.trimIndent(),
      textAfter = """
      <div
          class="value"
      // >
          Content
      </div>
      """.trimIndent(),
    )
  }

  // The compiler reads top-level <script>/<style> attributes with read_static_attribute, which accepts
  // no comments, so `//` must not be offered there.
  fun testTopLevelScriptTagHeaderStaysHtmlCommenter() {
    doCommentLineTest(
      textBefore = """
      <script
          <caret>lang="ts"
      >
      </script>
      """.trimIndent(),
      textAfter = """
      <script
      <!--    lang="ts"-->
      >
      </script>
      """.trimIndent(),
    )
  }

  fun testSingleLineTagLineCommentIsStillHtmlComment() {
    doCommentLineTest(
      textBefore = """
      <div class="value">Content<caret></div>
      """.trimIndent(),
      textAfter = """
      <!--<div class="value">Content</div>-->
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

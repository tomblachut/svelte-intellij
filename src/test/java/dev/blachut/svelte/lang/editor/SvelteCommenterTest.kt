// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.editor

import com.intellij.openapi.actionSystem.IdeActions.ACTION_COMMENT_BLOCK
import com.intellij.openapi.actionSystem.IdeActions.ACTION_COMMENT_LINE
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteCommenterTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
  @Test
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
  @Test
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

  @Test
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

package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.LanguageFolding
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.jetbrains.annotations.NotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class SvelteEditorTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  private val basePath = "dev/blachut/svelte/lang/editor"

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
  }

  private val fakeFoldingBuilder = object : FoldingBuilder {
    override fun buildFoldRegions(@NotNull node: ASTNode, @NotNull document: Document): Array<FoldingDescriptor> {
      return FoldingDescriptor.EMPTY_ARRAY
    }

    override fun getPlaceholderText(@NotNull node: ASTNode): String? {
      return "..."
    }

    override fun isCollapsedByDefault(@NotNull node: ASTNode): Boolean {
      return false
    }
  }

  @Test
  fun testEnterBetweenSvelteTags() {
    myFixture.configureByText("Foo.svelte", "{#if test}<caret>{/if}")
    myFixture.type('\n')
    myFixture.checkResult("""
      {#if test}
          <caret>
      {/if}
    """.trimIndent()
    )
  }

  @Test
  fun testEnterBetweenHtmlTags() {
    myFixture.configureByText("Foo.svelte", "<div><caret></div>")
    myFixture.type('\n')
    myFixture.checkResult("""
      <div>
          <caret>
      </div>
    """.trimIndent())
  }

  @Test
  fun testCompleteSvelteTag() {
    myFixture.configureByText("Foo.svelte", "{#if test<caret>")
    myFixture.type('}')
    myFixture.checkResult("{#if test}<caret>{/if}")
  }

  @Test
  fun testCompleteSvelteTagWithSlash() {
    myFixture.configureByText("Foo.svelte", "{#if test}hello{<caret>")
    myFixture.type('/')
    myFixture.checkResult("{#if test}hello{/if}<caret>")
  }

  @Test
  fun testCompleteSvelteTag3() {
    myFixture.configureByText("Foo.svelte", """{#if true}{<caret>}""")
    myFixture.type("/")
    myFixture.checkResult("""{#if true}{/if}<caret>""")
  }

  @Test
  fun testCompleteSvelteTagAcrossClauses() {
    myFixture.configureByText("Foo.svelte", """{#if true}{:else}{<caret>}""")
    myFixture.type("/")
    myFixture.checkResult("""{#if true}{:else}{/if}<caret>""")
  }

  @Test
  fun testFoldingSvelteTag() {
    doTestFolding()
  }

  @Test
  fun testFoldingEmptySvelteTag() {
    doTestFolding()
  }

  @Test
  fun testFoldingSvelteTagInHtmlTag() {
    doTestFolding()
  }

  @Test
  fun testFoldingEditorFold() {
    try {
      // some plugins contribute additional builders for XMLLanguage.INSTANCE,
      // that leads to XML using CompositeFoldingBuilder, which we mock here;
      // with the addition of LSP Folding support, Svelte started to also use CompositeFoldingBuilder,
      // which actually fixes bugs around CompositeFoldingBuilder interop with multi-language trees.
      LanguageFolding.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, fakeFoldingBuilder)
      doTestFolding()
    }
    finally {
      LanguageFolding.INSTANCE.removeExplicitExtension(XMLLanguage.INSTANCE, fakeFoldingBuilder)
    }
  }

  private fun doTestFolding() {
    myFixture.testFoldingWithCollapseStatus(myFixture.testDataPath + "/" + basePath + "/" + testName + ".svelte")
  }

  // WEB-77758: a `/` typed mid-header of a closed tag may begin a comment, so it inserts plainly --
  // in particular it must not break the header and re-close the already closed tag with a second `>`.
  @Test
  fun testSlashInsertsPlainlyMidHeader() {
    myFixture.configureByText("Foo.svelte", "<div <caret> ></div>")
    myFixture.type('/')
    myFixture.checkResult("<div /<caret> ></div>")
  }

  // Directly at the tag end the platform assists stay, matching JSX.
  @Test
  fun testSlashConvertsEmptyTagToSelfClosing() {
    myFixture.configureByText("Foo.svelte", "<div <caret>></div>")
    myFixture.type('/')
    myFixture.checkResult("<div /<caret>>")
  }

  @Test
  fun testSlashTypesOverEmptyElementEnd() {
    myFixture.configureByText("Foo.svelte", "<Foo <caret>/>")
    myFixture.type('/')
    myFixture.checkResult("<Foo /<caret>>")
  }

  @Test
  fun testTagHeaderCommentInClosedTag() {
    myFixture.configureByText("Foo.svelte", "<div <caret>>x</div>")
    myFixture.type("// c")
    myFixture.checkResult("<div // c<caret>>x</div>")
  }

  @Test
  fun testSlashInsertsPlainlyBetweenAttributes() {
    myFixture.configureByText("Foo.svelte", """<div <caret> class="x">t</div>""")
    myFixture.type('/')
    myFixture.checkResult("""<div /<caret> class="x">t</div>""")
  }

  @Test
  fun testLineCommentTypedOnOwnHeaderLine() {
    myFixture.configureByText("Foo.svelte", "<div \n<caret>\n>\n    \n</div>")
    myFixture.type("// c")
    myFixture.checkResult("<div \n// c<caret>\n>\n    \n</div>")
  }

  // An unclosed tag keeps the platform `/` -> `/>` auto-close.
  @Test
  fun testSlashAutoClosesUnclosedTag() {
    myFixture.configureByText("Foo.svelte", "<div <caret>")
    myFixture.type('/')
    myFixture.checkResult("<div /><caret>")
  }

  // The `</` end-tag completion must keep working.
  @Test
  fun testSlashStillCompletesEndTag() {
    myFixture.configureByText("Foo.svelte", "<div>x<<caret>")
    myFixture.type('/')
    myFixture.checkResult("<div>x</div><caret>")
  }
}
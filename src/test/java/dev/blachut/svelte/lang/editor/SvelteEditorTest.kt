package dev.blachut.svelte.lang.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.LanguageFolding
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.SvelteHTMLLanguage
import dev.blachut.svelte.lang.getSvelteTestDataPath
import org.jetbrains.annotations.NotNull

class SvelteEditorTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()
  override fun getBasePath(): String = "dev/blachut/svelte/lang/editor"

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
    myFixture.testFoldingWithCollapseStatus(testDataPath + "/" + basePath + "/" + getTestName(false) + ".svelte")
  }
}
package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteBraceTypedTest {
    companion object {
        private val projectFixture = svelteProjectFixture()
    }

    private val myFixture by svelteCodeInsightFixture(projectFixture)

    @Test
    fun testBracesClosingInText() {
        myFixture.initTest("""prefix<caret>suffix""")
        myFixture.type("{")
        myFixture.checkResult("""prefix{<caret>}suffix""")
    }

    @Test
    fun testBracesClosingInTextBackspace() {
        myFixture.initTest("""prefix{<caret>}suffix""")
        myFixture.type("\b")
        myFixture.checkResult("""prefix<caret>suffix""")
    }

    @Test
    fun testBracesClosingBeforeSvelteTag() {
        myFixture.initTest("""<caret>{#if true}{/if}""")
        myFixture.type("{")
        myFixture.checkResult("""{<caret>}{#if true}{/if}""")
    }

    @Test
    fun testBracesClosingBeforeSvelteTagBackspace() {
        myFixture.initTest("""{<caret>}{#if true}{/if}""")
        myFixture.type("\b")
        myFixture.checkResult("""<caret>{#if true}{/if}""")
    }

    @Test
    fun testBracesClosingInHtmlTag() {
        myFixture.initTest("""<div <caret>>""")
        myFixture.type("{")
        myFixture.checkResult("""<div {<caret>}>""")
    }

    @Test
    fun testBracesClosingInHtmlTagBackspace() {
        myFixture.initTest("""<div {<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div <caret>>""")
    }

    @Test
    fun testBracesClosingInHtmlAttribute() {
        myFixture.initTest("""<div test=<caret>>""")
        myFixture.type("{")
        myFixture.checkResult("""<div test={<caret>}>""")
    }

    @Test
    fun testBracesClosingInHtmlAttributeBackspace() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test=<caret>>""")
    }

    @Test
    fun testBracesClosingInHtmlAttributeQuotes() {
        myFixture.initTest("""<div test="<caret>">""")
        myFixture.type("{")
        myFixture.checkResult("""<div test="{<caret>}">""")
    }

    @Test
    fun testBracesClosingInHtmlAttributeQuotesBackspace() {
        myFixture.initTest("""<div test="{<caret>}">""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test="<caret>">""")
    }

    @Test
    fun testBracesClosingInHtmlAttributeObjectLiteral() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("{")
        myFixture.checkResult("""<div test={{<caret>}}>""")
    }

    @Test
    fun testBracesClosingInHtmlAttributeObjectLiteralBackspace() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test=<caret>>""")
    }

    private fun CodeInsightTestFixture.initTest(text: String) {
        this.configureByText("Foo.svelte", text)
    }
}

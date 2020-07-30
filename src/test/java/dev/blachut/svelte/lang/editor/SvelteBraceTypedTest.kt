package dev.blachut.svelte.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

class SvelteBraceTypedTest : BasePlatformTestCase() {
    fun testBracesClosingInText() {
        myFixture.initTest("""prefix<caret>suffix""")
        myFixture.type("{")
        myFixture.checkResult("""prefix{<caret>}suffix""")
    }

    fun testBracesClosingInTextBackspace() {
        myFixture.initTest("""prefix{<caret>}suffix""")
        myFixture.type("\b")
        myFixture.checkResult("""prefix<caret>suffix""")
    }

    fun testBracesClosingBeforeSvelteTag() {
        myFixture.initTest("""<caret>{#if true}{/if}""")
        myFixture.type("{")
        myFixture.checkResult("""{<caret>}{#if true}{/if}""")
    }

    fun testBracesClosingBeforeSvelteTagBackspace() {
        myFixture.initTest("""{<caret>}{#if true}{/if}""")
        myFixture.type("\b")
        myFixture.checkResult("""<caret>{#if true}{/if}""")
    }

    fun testBracesClosingInHtmlTag() {
        myFixture.initTest("""<div <caret>>""")
        myFixture.type("{")
        myFixture.checkResult("""<div {<caret>}>""")
    }

    fun testBracesClosingInHtmlTagBackspace() {
        myFixture.initTest("""<div {<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div <caret>>""")
    }

    fun testBracesClosingInHtmlAttribute() {
        myFixture.initTest("""<div test=<caret>>""")
        myFixture.type("{")
        myFixture.checkResult("""<div test={<caret>}>""")
    }

    fun testBracesClosingInHtmlAttributeBackspace() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test=<caret>>""")
    }

    fun testBracesClosingInHtmlAttributeQuotes() {
        myFixture.initTest("""<div test="<caret>">""")
        myFixture.type("{")
        myFixture.checkResult("""<div test="{<caret>}">""")
    }

    fun testBracesClosingInHtmlAttributeQuotesBackspace() {
        myFixture.initTest("""<div test="{<caret>}">""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test="<caret>">""")
    }

    fun testBracesClosingInHtmlAttributeObjectLiteral() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("{")
        myFixture.checkResult("""<div test={{<caret>}}>""")
    }

    fun testBracesClosingInHtmlAttributeObjectLiteralBackspace() {
        myFixture.initTest("""<div test={<caret>}>""")
        myFixture.type("\b")
        myFixture.checkResult("""<div test=<caret>>""")
    }

    private fun CodeInsightTestFixture.initTest(text: String) {
        this.configureByText("Foo.svelte", text)
    }
}

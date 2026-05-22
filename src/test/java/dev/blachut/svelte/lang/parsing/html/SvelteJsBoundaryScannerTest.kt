package dev.blachut.svelte.lang.parsing.html

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteJsBoundaryScannerTest : BasePlatformTestCase() {
  fun testPlainIdentifierReturnsOffsetOfClosingBrace() {
    // Buffer: "foo}rest"
    //         0123456
    // Expected: scanner returns 3 (the `}`)
    val buf = "foo}rest"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(3, result)
  }

  fun testSingleQuotedStringWithEmbeddedBrace() {
    val buf = "'a}b'}"
    //         012345
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(5, result)
  }

  fun testDoubleQuotedStringWithEmbeddedBrace() {
    val buf = "\"a}b\"}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(5, result)
  }

  fun testTemplateLiteralWithoutSubstitution() {
    val buf = "`a}b`}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(5, result)
  }

  fun testTemplateLiteralWithSubstitutionBalancesInternally() {
    // `a${b}c` followed by Svelte's }
    // Internal ${b}: DOLLAR LBRACE IDENTIFIER RBRACE — depth 0->1->0
    // External }: depth 0 -> return offset
    val buf = "`a\${b}c`}"
    //         01 2345678
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(8, result)
  }

  fun testTemplateLiteralWithObjectLiteralInSubstitution() {
    val buf = "`a\${ {x: 1}.x }c`}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testRegexLiteralWithEmbeddedQuote() {
    // /"/g — the regex contains a quote; depth-counter must not see RBRACE here
    val buf = "x.replace(/\"/g, '')}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testRegexLiteralWithCharacterClassContainingSlash() {
    val buf = "x.replace(/[/]/g, '-')}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testRegexLiteralWithEscapedDelimiter() {
    val buf = "x.replace(/\\//g, '-')}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testDivisionChainNotMisreadAsRegex() {
    val buf = "a / b / c}"
    //         0123456789
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(9, result)
  }

  fun testLineCommentContainingSlashesAndBrace() {
    // The newline lets the comment terminate; the } after newline is the terminator
    val buf = "// /api/foo/ \nx}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testBlockCommentWithEmbeddedBrace() {
    val buf = "x /* } */ + y}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testMultipleRegexLiteralsOnOneLine() {
    val buf = "x.replace(/\"/g, \"\").replace(/'/g, \"\")}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testJsxSelfClosingTag() {
    val buf = "<Foo />}"
    //         01234567
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(7, result)
  }

  fun testJsxWithExpressionContainerIsBalancedInternally() {
    // {nested} inside <Foo> uses XML_LBRACE/XML_RBRACE — scanner ignores
    val buf = "<Foo>{nested}</Foo>}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testJsxAttributeWithExpressionContainer() {
    val buf = "<Foo attr={value} />}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testComparisonLessThanIsNotJsx() {
    val buf = "a < b}"
    //         012345
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(5, result)
  }

  fun testComparisonGreaterThanIsNotJsx() {
    val buf = "a > b}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(5, result)
  }

  fun testEmptyBufferReturnsStart() {
    // Scanner called with start=0 on a buffer where buf[0] == '}'
    val buf = "}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(0, result)
  }

  fun testUnterminatedReturnsEnd() {
    val buf = "foo + bar"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length, result)
  }

  fun testNestedObjectLiteralBalancesInternally() {
    val buf = "a + { x: 1 }.x}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testNestedArrowFunctionBlockBalancesInternally() {
    val buf = "(() => { return 1 })()}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testRespectsStartOffset() {
    // Same buffer, but scan only from offset 4
    val buf = "skip foo}rest"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 4, buf.length)
    assertEquals(8, result)
  }

  fun testRespectsEndOffset() {
    // Don't scan past offset 7 — even though there's a } at offset 11
    val buf = "no brace here }after"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, 7)
    assertEquals(7, result)
  }

  fun testLazyReparseBufferWithOuterBracesReturnsEnd() {
    // Scanner called from inside a lazy reparse where the buffer is the full
    // SVELTE_EXPRESSION text including the outer `{` and `}`. The opening `{`
    // is counted as depth-opening; the matching `}` brings depth back to 0
    // without being returned; scanner returns end.
    val buf = "{foo}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length, result)
  }

  fun testTsTypeAssertionAsTypeIsIdentifierShaped() {
    val buf = "x as number}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testTsSatisfiesOperatorIsIdentifierShaped() {
    val buf = "obj satisfies SomeType}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testTsGenericArrowWithCommaDisambiguator() {
    // §2.3 risk validation: <T,>(x: T) => x
    // JS_WITH_JSX scanner sees < as potentially-JSX. The comma after T should
    // either disambiguate as TS generic, or JS lexer recovers without losing
    // the closing }. If this test fails, escalate to dialect-aware scanner
    // (see spec §7 mitigation).
    val buf = "<T,>(x: T) => x}"
    val result = SvelteJsBoundaryScanner.findUnbalancedRbrace(buf, 0, buf.length)
    assertEquals(buf.length - 1, result)
  }

  fun testHostLexerCrashFile_NoOversizedCodeFragment() {
    val text = """<script lang="ts">
    let isDark = false
</script>
<div class="layout" class:dark={isDark}>
    <button onclick="
    {@render children()}
</div>
<SnippenComponent>
    {#snippet title()}My Title{/snippet}
    {#snippet body()}My Body{/snippet}
</SnippenComponent>
<style>
    .layout { padding: 20px; transition: background 0.3s; }
    .dark { background: #333; color: white; }
</style>
"""
    val lexer = SvelteHtmlLexer(false)
    lexer.start(text)
    var maxFragmentLen = 0
    while (lexer.tokenType != null) {
      if (lexer.tokenType == dev.blachut.svelte.lang.psi.SvelteTokenTypes.CODE_FRAGMENT) {
        maxFragmentLen = maxOf(maxFragmentLen, lexer.tokenEnd - lexer.tokenStart)
      }
      lexer.advance()
    }
    // Pre-fix, one CODE_FRAGMENT was 192 chars (engulfed snippet blocks, closing
    // tags, and the entire <style> block). All fragments should now be small
    // expression-sized chunks; we assert <50 as a generous ceiling.
    assertTrue("Expected all CODE_FRAGMENT spans to be small; got max=$maxFragmentLen",
               maxFragmentLen < 50)
  }

  // --- Fix: leading `/` skip
  fun testLeadingSlashThenKeyword() = assertReturnsAt("/snippet}r", 8)
  fun testLeadingSlashThenIdent() = assertReturnsAt("/foo}r", 4)

  // --- Fix: whitespace before the leading `/`
  fun testLeadingSpaceThenSlash() = assertReturnsAt(" /snippet}r", 9)
  fun testLeadingNewlineThenSlash() = assertReturnsAt("\n/snippet}r", 9)
  fun testLeadingTabThenSlash() = assertReturnsAt("\t/snippet}r", 9)
  fun testLeadingSpaceThenSlashThenIdent() = assertReturnsAt(" /foo}r", 5)
  fun testLeadingFormFeedThenSlash() = assertReturnsAt("\u000C/foo}r", 5)
  fun testLeadingLineSeparatorThenSlash() = assertReturnsAt("\u2028/foo}r", 5)

  // --- Fix: Svelte-aligned `/` condition (skip unless followed by `/` or `*`)
  fun testLeadingSlashThenDigit() = assertReturnsAt("/2}r", 2)
  fun testLeadingSlashThenCloseBrace() = assertReturnsAt("/}r", 1)
  fun testLeadingSlashThenOperator() = assertReturnsAt("/+x}r", 3)

  // --- Preserved: comments and regex literals lex via the JS lexer
  fun testLeadingRegexLiteralTerminatesAtSecondSlash() = assertReturnsAt("/abc/g}r", 6)
  fun testLeadingBlockComment() = assertReturnsAt("/* c */}r", 7)
  fun testLeadingLineCommentTerminatesAtNewline() = assertReturnsAt("//c\n}r", 4)
  fun testLeadingDqStringTerminatesAtNewline() = assertReturnsAt("\"unterm\n }r", 9)

  // --- Preserved: non-JS-prefix chars lex without overshoot
  fun testLeadingHash() = assertReturnsAt("#foo}r", 4)
  fun testLeadingColon() = assertReturnsAt(":foo}r", 4)
  fun testLeadingAt() = assertReturnsAt("@foo}r", 4)
  fun testLeadingMemberAccess() = assertReturnsAt(".x}r", 2)
  fun testLeadingAssignmentOp() = assertReturnsAt("=x}r", 2)
  fun testLeadingUnaryBang() = assertReturnsAt("!x}r", 2)
  fun testLeadingBackslash() = assertReturnsAt("\\x}r", 2)

  private fun assertReturnsAt(input: String, expected: Int) {
    val actual = SvelteJsBoundaryScanner.findUnbalancedRbrace(input, 0, input.length)
    val shown = input.replace("\n", "\\n").replace("\t", "\\t")
    assertEquals("Scanner mismatch for input \"$shown\"", expected, actual)
  }
}

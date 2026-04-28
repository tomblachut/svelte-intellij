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
}

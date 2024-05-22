package dev.blachut.svelte.lang

import dev.blachut.svelte.lang.codeInsight.*
import dev.blachut.svelte.lang.editor.*
import dev.blachut.svelte.lang.format.SvelteFormatterTest
import dev.blachut.svelte.lang.parsing.html.*
import dev.blachut.svelte.lang.service.SvelteServiceCompletionTest
import dev.blachut.svelte.lang.service.SvelteServiceDocumentationTest
import dev.blachut.svelte.lang.service.SvelteServiceTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  // Parsing
  SvelteHtmlRegressionParsingTest::class,
  SvelteHighlightingLexerTest::class,
  SvelteHtmlParserTest::class,
  SvelteDirectiveLexerTest::class,
  SvelteDirectiveParserTest::class,
  // Editing
  SvelteFormatterTest::class,
  SvelteBraceTypedTest::class,
  SvelteCommenterTest::class,
  SvelteEditorTest::class,
  SvelteEmmetTest::class,
  SvelteSelectWordTest::class,
  // Code insight
  SvelteAutoPopupTest::class,
  SvelteCompletionTest::class,
  SvelteHighlightingTest::class,
  SvelteRenameTest::class,
  SvelteResolveTest::class,
  SvelteNavigationTest::class,
  SvelteBreadcrumbsTest::class,

  SvelteTypeScriptCompletionTest::class,
  SvelteTypeScriptHighlightingTest::class,
  SvelteCopyPasteTest::class,
  SvelteCreateVariableTest::class,
  SvelteCreateFunctionTest::class,
  SvelteCreateImportTest::class,
  SvelteKitTest::class,
)
class SvelteAggregatorTestSuite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  SvelteServiceTest::class,
  SvelteServiceCompletionTest::class,
  SvelteServiceDocumentationTest::class,
)
class SvelteServiceTestSuite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  SvelteAggregatorTestSuite::class,
  SvelteServiceTestSuite::class,
)
class SvelteTestSuite

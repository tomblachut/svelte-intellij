package dev.blachut.svelte.lang

import dev.blachut.svelte.lang.codeInsight.SvelteAutoPopupTest
import dev.blachut.svelte.lang.codeInsight.SvelteBreadcrumbsTest
import dev.blachut.svelte.lang.codeInsight.SvelteCompletionTest
import dev.blachut.svelte.lang.codeInsight.SvelteCopyPasteTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateFunctionTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateImportTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateVariableTest
import dev.blachut.svelte.lang.codeInsight.SvelteFindUsagesTest
import dev.blachut.svelte.lang.codeInsight.SvelteGenericsInspectionTest
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import dev.blachut.svelte.lang.codeInsight.SvelteKitTest
import dev.blachut.svelte.lang.codeInsight.SvelteNavigationTest
import dev.blachut.svelte.lang.codeInsight.SvelteRenameTest
import dev.blachut.svelte.lang.codeInsight.SvelteResolveTest
import dev.blachut.svelte.lang.codeInsight.SvelteTypeScriptCompletionTest
import dev.blachut.svelte.lang.codeInsight.SvelteTypeScriptHighlightingTest
import dev.blachut.svelte.lang.editor.SvelteBraceTypedTest
import dev.blachut.svelte.lang.editor.SvelteCommenterTest
import dev.blachut.svelte.lang.editor.SvelteEditorTest
import dev.blachut.svelte.lang.editor.SvelteEmmetTest
import dev.blachut.svelte.lang.editor.SvelteSelectWordTest
import dev.blachut.svelte.lang.format.SvelteFormatterTest
import dev.blachut.svelte.lang.parsing.html.SvelteDirectiveLexerTest
import dev.blachut.svelte.lang.parsing.html.SvelteDirectiveParserTest
import dev.blachut.svelte.lang.parsing.html.SvelteHighlightingLexerTest
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlParserTest
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlRegressionParsingTest
import dev.blachut.svelte.lang.service.SvelteNsRenameServiceTest
import dev.blachut.svelte.lang.service.SvelteNsUsagesServiceTest
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
  SvelteFindUsagesTest::class,
  SvelteBreadcrumbsTest::class,
  SvelteGenericsInspectionTest::class,

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
  SvelteNsUsagesServiceTest::class,
  SvelteNsRenameServiceTest::class,
)
class SvelteServiceTestSuite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  SvelteAggregatorTestSuite::class,
  SvelteServiceTestSuite::class,
)
class SvelteTestSuite

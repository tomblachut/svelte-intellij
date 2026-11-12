package dev.blachut.svelte.lang

import com.intellij.idea.IJIgnore
import dev.blachut.svelte.lang.codeInsight.SvelteBreadcrumbsTest
import dev.blachut.svelte.lang.codeInsight.SvelteCompletionTest
import dev.blachut.svelte.lang.codeInsight.SvelteCopyPasteTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateFunctionTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateImportTest
import dev.blachut.svelte.lang.codeInsight.SvelteCreateVariableTest
import dev.blachut.svelte.lang.codeInsight.SvelteGenericsInspectionTest
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import dev.blachut.svelte.lang.codeInsight.SvelteKitTest
import dev.blachut.svelte.lang.codeInsight.SvelteNavigationTest
import dev.blachut.svelte.lang.codeInsight.SvelteOptimizeImportsTest
import dev.blachut.svelte.lang.codeInsight.SvelteResolveTest
import dev.blachut.svelte.lang.codeInsight.SvelteTypeScriptCompletionTest
import dev.blachut.svelte.lang.codeInsight.SvelteTypeScriptHighlightingTest
import dev.blachut.svelte.lang.editor.SvelteBraceTypedTest
import dev.blachut.svelte.lang.editor.SvelteCommenterTest
import dev.blachut.svelte.lang.editor.SvelteEditorTest
import dev.blachut.svelte.lang.editor.SvelteEmmetTest
import dev.blachut.svelte.lang.editor.SvelteSelectWordTest
import dev.blachut.svelte.lang.parsing.html.SvelteDirectiveLexerTest
import dev.blachut.svelte.lang.parsing.html.SvelteDirectiveParserTest
import dev.blachut.svelte.lang.parsing.html.SvelteHighlightingLexerTest
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
  // Parsing
  SvelteHighlightingLexerTest::class,
  SvelteDirectiveLexerTest::class,
  SvelteDirectiveParserTest::class,
  // Editing
  SvelteBraceTypedTest::class,
  SvelteCommenterTest::class,
  SvelteEditorTest::class,
  SvelteEmmetTest::class,
  SvelteSelectWordTest::class,
  // Code insight
  SvelteCompletionTest::class,
  SvelteHighlightingTest::class,
  SvelteOptimizeImportsTest::class,
  SvelteResolveTest::class,
  SvelteNavigationTest::class,
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
@IJIgnore(issue = "Manual only")
class SvelteTestSuite

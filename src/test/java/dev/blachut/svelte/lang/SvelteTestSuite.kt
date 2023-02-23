package dev.blachut.svelte.lang

import dev.blachut.svelte.lang.codeInsight.*
import dev.blachut.svelte.lang.editor.SvelteBraceTypedTest
import dev.blachut.svelte.lang.editor.SvelteCommenterTest
import dev.blachut.svelte.lang.editor.SvelteEditorTest
import dev.blachut.svelte.lang.editor.SvelteEmmetTest
import dev.blachut.svelte.lang.format.SvelteFormatterTest
import dev.blachut.svelte.lang.parsing.html.*
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
    // Code insight
    SvelteAutoPopupTest::class,
    SvelteCompletionTest::class,
    SvelteHighlightingTest::class,
    SvelteResolveTest::class,
    SvelteNavigationTest::class,
    SvelteTypeScriptCompletionTest::class,
    SvelteTypeScriptHighlightingTest::class,
    SvelteCopyPasteTest::class,
    SvelteCreateVariableTest::class,
    SvelteCreateFunctionTest::class,
    SvelteCreateImportTest::class,
    SvelteKitTest::class,
)
class SvelteTestSuite

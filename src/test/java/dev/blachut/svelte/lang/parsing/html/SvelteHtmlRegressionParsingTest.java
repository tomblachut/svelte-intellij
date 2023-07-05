// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.parsing.html;

import com.intellij.html.HtmlParsingTest;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import dev.blachut.svelte.lang.SvelteHTMLLanguage;
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition;
import org.junit.AssumptionViolatedException;

import static dev.blachut.svelte.lang.SvelteTestUtilKt.getSvelteTestDataPath;

public class SvelteHtmlRegressionParsingTest extends HtmlParsingTest {
  public SvelteHtmlRegressionParsingTest() {
    super("dev/blachut/svelte/lang/parsing/html/regressionHtml",
          "svelte",
          new SvelteHTMLParserDefinition(),
          new SvelteJSParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return getSvelteTestDataPath();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExplicitExtension(LanguageASTFactory.INSTANCE, SvelteHTMLLanguage.INSTANCE, new SvelteHtmlASTFactory());
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
  }

  @Override
  public void testSpecialTagsParsing() {
    throw new AssumptionViolatedException("Ignore");
  }

  @Override
  public void testScriptWithinScript() {
    throw new AssumptionViolatedException("Ignore");
  }
}

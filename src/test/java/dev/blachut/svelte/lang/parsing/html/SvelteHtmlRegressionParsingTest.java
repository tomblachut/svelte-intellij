// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.parsing.html;

import com.intellij.javascript.testFramework.web.JSHtmlParsingTest;
import com.intellij.testFramework.junit5.TestApplication;
import dev.blachut.svelte.lang.parsing.js.SvelteJSParserDefinition;
import org.junit.jupiter.api.Test;

import static dev.blachut.svelte.lang.SvelteTestUtilKt.getSvelteTestDataPath;
import static org.junit.jupiter.api.Assumptions.abort;

@TestApplication
public class SvelteHtmlRegressionParsingTest extends JSHtmlParsingTest {
  public SvelteHtmlRegressionParsingTest() {
    super("svelte",
          new SvelteHTMLParserDefinition(),
          new SvelteJSParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return getSvelteTestDataPath() + "/dev/blachut/svelte/lang/parsing/html/regressionHtml";
  }

  @Test
  @Override
  public void testSpecialTagsParsing() {
    abort("Ignore");
  }

  @Test
  @Override
  public void testScriptWithinScript() {
    abort("Ignore");
  }
}

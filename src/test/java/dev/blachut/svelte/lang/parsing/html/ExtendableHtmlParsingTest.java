// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.parsing.html;

import com.intellij.lang.LanguageASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import dev.blachut.svelte.lang.SvelteHTMLLanguage;

/**
 * Copied over from intellij-community
 */
public class ExtendableHtmlParsingTest extends XmlParsingTest {
    public ExtendableHtmlParsingTest() {
        super("dev/blachut/svelte/lang/parsing/html/regressionHtml", "svelte", new SvelteHTMLParserDefinition());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addExplicitExtension(LanguageASTFactory.INSTANCE, SvelteHTMLLanguage.INSTANCE, new SvelteHtmlASTFactory());
        registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
    }

    @Override
    protected final void doTestXml(String text) throws Exception {
        doTestHtml(text);
    }

    protected void doTestHtml(String text) throws Exception {
        doTest(text, "test.html");
    }

    @Override
    public void testDtdUrl1() {
        //disable test
    }

    @Override
    public void testCustomMimeType() {
        //disable test
    }

    public void testHtmlDoctype1() throws Exception {
        doTestHtml("<!DOCTYPE html>\n");
    }

    public void testHtmlDoctype2() throws Exception {
        doTestHtml(" <!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
    }

    public void testHtmlDoctype3() throws Exception {
        doTestHtml(" <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
    }

    public void testHtmlCharEntityRef() throws Exception {
        doTestHtml("&#xAAff;&#XaaFF;&#x&#X<tag attr='&#xAAff;&#XaaFF;&#x&#X'/>");
    }

    public void testHtmlComments() throws Exception {
        doTestHtml("<!--Valid comment-->\n" +
            "<!--Valid comment<!-->\n" +
            "<!--Invalid content <!-- -->\n" +
            "<!--Invalid comment starts: --> <!--> <!--->\n" +
            "<!--Invalid end <!--->\n" +
            "<!--Invalid end --!>\n");
    }

    public void testHtmlIEConditionalComments1() throws Exception {
        doTestHtml("<!--[if IE 6]>\n" +
            "<p>You are using Internet Explorer 6.</p>\n" +
            "<![endif]-->");
    }

    public void testHtmlIEConditionalComments2() throws Exception {
        doTestHtml("<!--[if lte IE 7]>\n" +
            "<style type=\"text/css\">\n" +
            "/* CSS here */\n" +
            "</style>\n" +
            "<![endif]-->");
    }

    public void testHtmlIEConditionalComments3() throws Exception {
        doTestHtml("<!--[if !IE]>-->\n" +
            "<link href=\"non-ie.css\" rel=\"stylesheet\">\n" +
            "<!--<![endif]-->");
    }

    public void ignoreTestScriptEmbeddingParsing() throws Exception {
        doTestHtml("<script type=\"foo/bar\"><div></div></script>\n" +
            "<script type=\"foo/bar\"><div> </div></script>");
    }
}

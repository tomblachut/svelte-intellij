package dev.blachut.svelte.lang.parsing.html;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.*;
import com.intellij.psi.TokenType;
import dev.blachut.svelte.lang.psi.SvelteTokenTypes;
import com.intellij.lang.javascript.JSTokenTypes;

%%

%unicode

%{
  private static final int NO_QUOTE = 0;
  private static final int SINGLE_QUOTE = 1;
  private static final int DOUBLE_QUOTE = 2;
  private static final int BACKQUOTE = 3;

  public int bracesNestingLevel = 0;
  public int quoteMode = NO_QUOTE;

  public _SvelteHtmlRawTextLexer() {
    this((java.io.Reader)null);
  }

  public final void yybeginNestable(int state) {
      bracesNestingLevel = 0;
      quoteMode = NO_QUOTE;
      yybegin(state);
  }

  private void toggleQuoteMode(int mode) {
    if (quoteMode == NO_QUOTE) {
      quoteMode = mode;
    } else if (quoteMode == mode) {
      quoteMode = NO_QUOTE;
    }
  }
%}

%eof{
  bracesNestingLevel = 0;
  quoteMode = NO_QUOTE;
%eof}

%class _SvelteHtmlRawTextLexer
%public
%implements FlexLexer
%function advance
%type IElementType
//%debug

%state SVELTE_INTERPOLATION_START
%state SVELTE_INTERPOLATION_KEYWORD
%state SVELTE_INTERPOLATION

ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE=\s+
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+

TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*

SINGLE_QUOTE="'"
DOUBLE_QUOTE="\""
BACKQUOTE="`"
ESCAPED_QUOTES=\\'|\\\"|\\`

%%

<YYINITIAL> "{" { yybeginNestable(SVELTE_INTERPOLATION_START); return SvelteTokenTypes.START_MUSTACHE; }

<YYINITIAL> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_REAL_WHITE_SPACE; }

<SVELTE_INTERPOLATION_START> {
  {WHITE_SPACE}      { return TokenType.WHITE_SPACE; }
  "#"                { yybegin(SVELTE_INTERPOLATION_KEYWORD); return JSTokenTypes.SHARP; }
  ":"                { yybegin(SVELTE_INTERPOLATION_KEYWORD); return JSTokenTypes.COLON; }
  "/"                { yybegin(SVELTE_INTERPOLATION_KEYWORD); return JSTokenTypes.DIV; }
  "@"                { yybegin(SVELTE_INTERPOLATION_KEYWORD); return JSTokenTypes.AT; }
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

<SVELTE_INTERPOLATION_KEYWORD> {
  {WHITE_SPACE}      { return TokenType.WHITE_SPACE; }
  "if"               { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.IF_KEYWORD; }
  "else"             { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.ELSE_KEYWORD; }
  "each"             { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.EACH_KEYWORD; }
  "await"            { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.AWAIT_KEYWORD; }
  "then"             { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.THEN_KEYWORD; }
  "catch"            { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.CATCH_KEYWORD; }
  "key"              { yybegin(SVELTE_INTERPOLATION); return SvelteTokenTypes.KEY_KEYWORD; }
  ({ALPHA})+         { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

<SVELTE_INTERPOLATION> {
  {ESCAPED_QUOTES}   { return SvelteTokenTypes.CODE_FRAGMENT; }
  {SINGLE_QUOTE}     { toggleQuoteMode(SINGLE_QUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  {DOUBLE_QUOTE}     { toggleQuoteMode(DOUBLE_QUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  {BACKQUOTE}        { toggleQuoteMode(BACKQUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  "{"                { if (quoteMode == NO_QUOTE) { bracesNestingLevel++; } return SvelteTokenTypes.CODE_FRAGMENT; }
  "}"                {
                        if (quoteMode != NO_QUOTE) { return SvelteTokenTypes.CODE_FRAGMENT; }
                        if (bracesNestingLevel > 0) { bracesNestingLevel--; return SvelteTokenTypes.CODE_FRAGMENT; }
                        if (yystate() == SVELTE_INTERPOLATION) yybegin(YYINITIAL);
                        return SvelteTokenTypes.END_MUSTACHE;
                     }
  [^]                { return SvelteTokenTypes.CODE_FRAGMENT; }
}

<YYINITIAL> {
  "&lt;" |
  "&gt;" |
  "&apos;" |
  "&quot;" |
  "&nbsp;" |
  "&amp;" |
  "&#"{DIGIT}+";" |
  "&#"[xX]({DIGIT}|[a-fA-F])+";" { return XmlTokenType.XML_CHAR_ENTITY_REF; }
  "&"{TAG_NAME}";" { return XmlTokenType.XML_ENTITY_REF_TOKEN; }
}

[^] { return XmlTokenType.XML_DATA_CHARACTERS; }

package dev.blachut.svelte.lang.parsing.top;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;

%%

//%debug
%public
%class _SvelteLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%{
  private int bracesNestingLevel = 0;
  private int parens = 0;
  private int brackets = 0;

  private IElementType quotedToken;

  private Stack<Integer> stack = new Stack<>();

  private void pushState(int newState) {
    stack.push(yystate());
    yybegin(newState);
  }

  private void popState() {
    yybegin(stack.pop());
  }

  private IElementType beginQuote(int quoteState, IElementType token) {
      quotedToken = token;
      pushState(quoteState);
      return quotedToken;
  }

  private boolean notNestedCode() {
      return (bracesNestingLevel + parens + brackets) == 0;
  }

  private void resetCounters() {
      bracesNestingLevel = 0;
      parens = 0;
      brackets = 0;
  }
%}

%eof{
  resetCounters();
%eof}

WHITE_SPACE=\s+
ID=[$_a-zA-Z0-9]+
SINGLE_QUOTE="'"
DOUBLE_QUOTE="\""
TICKED_QUOTE="`"

%state SVELTE_TAG
%state SVELTE_INTERPOLATION_START
%state SVELTE_INTERPOLATION
%state VERBATIM_COMMENT
%state VERBATIM_HTML
%state HTML_TAG
%xstate ONLY_WHITESPACE
%xstate SINGLE_QUOTE
%xstate DOUBLE_QUOTE
%xstate TICKED_QUOTE

%%
<YYINITIAL> {
  "<!--"                  { yybegin(VERBATIM_COMMENT); return HTML_FRAGMENT; }
  "<script" | "<style"    { yybegin(VERBATIM_HTML); return HTML_FRAGMENT; }
  "<"                     { yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "{"                     { yybegin(SVELTE_INTERPOLATION_START); return START_MUSTACHE; }
}

<SVELTE_TAG> {
  {SINGLE_QUOTE}     { return beginQuote(SINGLE_QUOTE, CODE_FRAGMENT); }
  {DOUBLE_QUOTE}     { return beginQuote(DOUBLE_QUOTE, CODE_FRAGMENT); }
  {TICKED_QUOTE}     { return beginQuote(TICKED_QUOTE, CODE_FRAGMENT); }
}

<SVELTE_INTERPOLATION_START> {
  {WHITE_SPACE}      { return TEMP_PREFIX; }
  "#"                { return HASH; }
  ":"                { return COLON; }
  "/"                { return SLASH; }
  "@"                { return AT; }
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

// TODO Disallow whitespace
<SVELTE_INTERPOLATION> {
  "if"               { return LAZY_IF; }
  "else"             { return LAZY_ELSE; }
  "each"             { return LAZY_EACH; }
  "await"            { return LAZY_AWAIT; }
  "then"             { return LAZY_THEN; }
  "catch"            { return LAZY_CATCH; }
  "{"                { bracesNestingLevel++; return CODE_FRAGMENT; }
  "}"                { if (bracesNestingLevel == 0) { yybegin(YYINITIAL); return END_MUSTACHE; } else { bracesNestingLevel--; return CODE_FRAGMENT; } }
  [^]                { return CODE_FRAGMENT; }
}

<VERBATIM_COMMENT> "-->"                 { yybegin(YYINITIAL); return HTML_FRAGMENT; }
<VERBATIM_HTML> "</script>" | "</style>" { yybegin(YYINITIAL); return HTML_FRAGMENT; }

<HTML_TAG> {
  {SINGLE_QUOTE}              { return beginQuote(SINGLE_QUOTE, HTML_FRAGMENT); }
  {DOUBLE_QUOTE}              { return beginQuote(DOUBLE_QUOTE, HTML_FRAGMENT); }
  ">"                         { yybegin(YYINITIAL); return HTML_FRAGMENT; }
}

<SINGLE_QUOTE> {SINGLE_QUOTE} { popState(); return quotedToken; }
<DOUBLE_QUOTE> {DOUBLE_QUOTE} { popState(); return quotedToken; }
<TICKED_QUOTE> {TICKED_QUOTE} { popState(); return quotedToken; }

<SINGLE_QUOTE, DOUBLE_QUOTE, TICKED_QUOTE> {
  \\[^]        { return quotedToken; }
  [^]          { return quotedToken; }
}

[^]                           { return HTML_FRAGMENT; }

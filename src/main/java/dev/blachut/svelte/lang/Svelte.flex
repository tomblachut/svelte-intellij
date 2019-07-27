package dev.blachut.svelte.lang;

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
  private char quote;
  private int braces;
  private int parens;

  private int stateAfterWs;

  private void eatWsThenBegin(int nextState) {
      stateAfterWs = nextState;
      yybegin(ONLY_WHITESPACE);
  }

  private void restoreState() {
      yybegin(stateAfterWs);
  }
%}

%eof{
  braces = 0;
  parens = 0;
%eof}

WHITE_SPACE=\s+
ID=[$_a-zA-Z0-9]+

%xstate ONLY_WHITESPACE
%state VERBATIM_COMMENT
%state VERBATIM_HTML
%state HTML_TAG
%state TAG_STRING
%state SVELTE_INTERPOLATION_PRE
%state SVELTE_INTERPOLATION
%state SVELTE_TAG_PRE
%state SVELTE_TAG
%state SVELTE_ELSE_TAG
%state SVELTE_TAG_PAREN_AWARE

%%
<YYINITIAL> {
  "<!--"                  { yybegin(VERBATIM_COMMENT); return HTML_FRAGMENT; }
  "<script" | "<style"    { yybegin(VERBATIM_HTML); return HTML_FRAGMENT; }
  "<"                     { yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "{"\s*"#"               { yybegin(SVELTE_TAG_PRE); return START_OPENING_MUSTACHE; }
  "{"\s*":"               { yybegin(SVELTE_TAG_PRE); return START_INNER_MUSTACHE; }
  "{"\s*"/"               { yybegin(SVELTE_TAG_PRE); return START_CLOSING_MUSTACHE; }
  "{"                     { yybegin(SVELTE_INTERPOLATION_PRE); return START_MUSTACHE; }
}

<ONLY_WHITESPACE> {
  {WHITE_SPACE}      { return WHITE_SPACE; }
  [^]                { restoreState(); yypushback(1); }
}

<SVELTE_TAG_PRE> {
  "if"               { yybegin(SVELTE_TAG); return IF; }
  "each"             { yybegin(SVELTE_TAG); return EACH; }
  "await"            { yybegin(SVELTE_TAG); return AWAIT; }
  "then"             { yybegin(SVELTE_TAG); return THEN; }
  "catch"            { yybegin(SVELTE_TAG); return CATCH; }
  "else"             { eatWsThenBegin(SVELTE_ELSE_TAG); return ELSE; }
  {ID}               { yybegin(SVELTE_TAG); return BAD_CHARACTER; }
  {WHITE_SPACE}      { return BAD_CHARACTER; }
}

<SVELTE_ELSE_TAG> {
  "if"               { yybegin(SVELTE_TAG); return IF; }
  {ID}               { yybegin(SVELTE_TAG); return CODE_FRAGMENT; }
}

<SVELTE_TAG, SVELTE_TAG_PAREN_AWARE> {
  "then"             { return THEN; }
  "as"               { yybegin(SVELTE_TAG_PAREN_AWARE); return AS; }
  ","                { if (braces == 0) { return COMMA; } else { return CODE_FRAGMENT; } }

  {WHITE_SPACE}      { if (braces == 0) { return WHITE_SPACE; } else { return CODE_FRAGMENT; } }
  {ID}("then"|"as"){ID}           { return CODE_FRAGMENT; }
  ("then"|"as"){ID}               { return CODE_FRAGMENT; }
  {ID}("then"|"as")               { return CODE_FRAGMENT; }
}

/*
    Key expressions are wrapped in parens and can contain any number of paren pairs. Wrapping parens need to be distinguished.
 */
<SVELTE_TAG_PAREN_AWARE> {
  "("                { parens += 1; if (parens == 1) { return START_PAREN; } else { return CODE_FRAGMENT; } }
  ")"                { parens -= 1; if (parens == 0) { return END_PAREN; } else { return CODE_FRAGMENT; } }
}

<SVELTE_TAG, SVELTE_ELSE_TAG, SVELTE_TAG_PAREN_AWARE> {
  "{"                { braces += 1; return CODE_FRAGMENT; }
  "}"                { if (braces == 0) { eatWsThenBegin(YYINITIAL); return END_MUSTACHE; } else { braces -= 1; return CODE_FRAGMENT; } }

  [^]                { return CODE_FRAGMENT; }
}

<SVELTE_INTERPOLATION_PRE> {
  {WHITE_SPACE}/"@"  { return WHITE_SPACE; }
  "@html"            { yybegin(SVELTE_INTERPOLATION); return HTML_PREFIX; }
  "@debug"           { yybegin(SVELTE_INTERPOLATION); return DEBUG_PREFIX; }
  "@" | "@"{ID}      { yybegin(SVELTE_INTERPOLATION); return BAD_CHARACTER; }
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

<SVELTE_INTERPOLATION> {
  "{"                { braces++; return CODE_FRAGMENT; }
  "}"                { if (braces == 0) { yybegin(YYINITIAL); return END_MUSTACHE; } else { braces--; return CODE_FRAGMENT; } }
  [^]                { return CODE_FRAGMENT; }
}

<VERBATIM_COMMENT> {
  "-->"                       { yybegin(YYINITIAL); return HTML_FRAGMENT; }
}

<VERBATIM_HTML> {
  "</script>" | "</style>"    { yybegin(YYINITIAL); return HTML_FRAGMENT; }
}

<HTML_TAG> {
  "'"                         { yybegin(TAG_STRING); quote = '\''; return HTML_FRAGMENT; }
  "\""                        { yybegin(TAG_STRING); quote = '"'; return HTML_FRAGMENT; }
  ">"                         { yybegin(YYINITIAL); return HTML_FRAGMENT; }
}

<TAG_STRING> {
  "'"                         { if (quote == '\'') yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "\""                        { if (quote == '"') yybegin(HTML_TAG); return HTML_FRAGMENT; }
}

[^]                           { return HTML_FRAGMENT; }

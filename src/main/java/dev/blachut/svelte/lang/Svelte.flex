package dev.blachut.svelte.lang;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;

%%

%{
  private char quote;
  private int leftBraceCount;
  private int leftParenCount;
%}

//%debug
%public
%class _SvelteLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%eof{
  leftBraceCount = 0;
  leftParenCount = 0;
%eof}

WHITE_SPACE=\s+

%state VERBATIM_COMMENT
%state VERBATIM_HTML
%state HTML_TAG
%state TAG_STRING
%state SVELTE_INTERPOLATION
%state SVELTE_TAG
%state SVELTE_TAG_PAREN_AWARE

%%
<YYINITIAL> {
  "<!--"                  { yybegin(VERBATIM_COMMENT); return HTML_FRAGMENT; }
  "<script" | "<style"    { yybegin(VERBATIM_HTML); return HTML_FRAGMENT; }
  "<"                     { yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "{" / \s*[#:/]          { yybegin(SVELTE_TAG); return START_MUSTACHE; }
  "{"                     { yybegin(SVELTE_INTERPOLATION); return START_MUSTACHE; }
  {WHITE_SPACE}           { return WHITE_SPACE; }
}

<SVELTE_TAG, SVELTE_TAG_PAREN_AWARE> {
  "#if"              { return IF; }
  "if"               { return ELSE_IF; }
  "/if"              { return END_IF; }

  "#each"            { return EACH; }
  "as"               { yybegin(SVELTE_TAG_PAREN_AWARE); return AS; }
  ","                { if (leftBraceCount == 0) { return COMMA; } else { return CODE_FRAGMENT; } }
  "/each"            { return END_EACH; }

  "#await"           { return AWAIT; }
  "then"             { return AWAIT_THEN; }
  ":then"            { return THEN; }
  ":catch"           { return CATCH; }
  "/await"           { return AWAIT_END; }

  ":else"            { return ELSE; }
  {WHITE_SPACE}      { if (leftBraceCount == 0) { return WHITE_SPACE; } else { return CODE_FRAGMENT; } }
}

<SVELTE_TAG_PAREN_AWARE> {
  "("                { leftParenCount += 1; if (leftParenCount == 1) { return START_PAREN; } else { return CODE_FRAGMENT; } }
  ")"                { leftParenCount -= 1; if (leftParenCount == 0) { return END_PAREN; } else { return CODE_FRAGMENT; } }
}

<SVELTE_INTERPOLATION, SVELTE_TAG, SVELTE_TAG_PAREN_AWARE> {
  "{"                { leftBraceCount += 1; return CODE_FRAGMENT; }
  "}"                { if (leftBraceCount == 0) { yybegin(YYINITIAL); return END_MUSTACHE; } else { leftBraceCount -= 1; return CODE_FRAGMENT; } }

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
package dev.blachut.svelte.lang;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
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
ID=[$_a-zA-Z0-9]+

%state VERBATIM_COMMENT
%state VERBATIM_HTML
%state HTML_TAG
%state TAG_STRING
%state SVELTE_INTERPOLATION_PRE
%state SVELTE_INTERPOLATION
%state SVELTE_TAG_PRE
%state SVELTE_TAG
%state SVELTE_TAG_PAREN_AWARE

%%
<YYINITIAL> {
  "<!--"                  { yybegin(VERBATIM_COMMENT); return HTML_FRAGMENT; }
  "<script" | "<style"    { yybegin(VERBATIM_HTML); return HTML_FRAGMENT; }
  "<"                     { yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "{"\s*"#"               { yybegin(SVELTE_TAG_PRE); return START_OPENING_MUSTACHE; }
  "{"\s*":"               { yybegin(SVELTE_TAG_PRE); return START_INNER_MUSTACHE; }
  "{"\s*"/"               { yybegin(SVELTE_TAG_PRE); return START_CLOSING_MUSTACHE; }
  "{"                     { yybegin(SVELTE_INTERPOLATION); return START_MUSTACHE; }
  {WHITE_SPACE}           { return WHITE_SPACE; }
}

<SVELTE_TAG_PRE> {
  "if"               { yybegin(SVELTE_TAG); return IF; }
  "each"             { yybegin(SVELTE_TAG); return EACH; }
  "await"            { yybegin(SVELTE_TAG); return AWAIT; }
  "then"             { yybegin(SVELTE_TAG); return THEN; }
  "catch"            { yybegin(SVELTE_TAG); return CATCH; }
  "else"             { yybegin(SVELTE_TAG); return ELSE; }
  {ID}               { yybegin(SVELTE_TAG); return BAD_CHARACTER; }
  {WHITE_SPACE}      { return BAD_CHARACTER; }
}
<SVELTE_TAG, SVELTE_TAG_PAREN_AWARE> {
  "if"               { return IF; }
  "then"             { return THEN; }
  "as"               { yybegin(SVELTE_TAG_PAREN_AWARE); return AS; }
  ","                { if (leftBraceCount == 0) { return COMMA; } else { return CODE_FRAGMENT; } }

  {WHITE_SPACE}      { if (leftBraceCount == 0) { return WHITE_SPACE; } else { return CODE_FRAGMENT; } }
  ("if"|"then"|"as"){ID}           { return BAD_CHARACTER; }
}

<SVELTE_TAG_PAREN_AWARE> {
  "("                { leftParenCount += 1; if (leftParenCount == 1) { return START_PAREN; } else { return CODE_FRAGMENT; } }
  ")"                { leftParenCount -= 1; if (leftParenCount == 0) { return END_PAREN; } else { return CODE_FRAGMENT; } }
}

<SVELTE_INTERPOLATION> {
  "@html"            { return HTML_PREFIX; }
  "@debug"           { return DEBUG_PREFIX; }
  "@"{ID}            { return BAD_CHARACTER; }
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
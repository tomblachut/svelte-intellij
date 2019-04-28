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
//  private int leftParenCount;

//  private Stack<Integer> stack = new Stack<>();
//
//  public void yypushState(int newState) {
//    stack.push(yystate());
//    yybegin(newState);
//  }
//
//  public void yypopState() {
//    yybegin(stack.pop());
//  }
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
//  leftParenCount = 0;
%eof}

WHITE_SPACE=\s+

%state VERBATIM_HTML
%state HTML_TAG
%state TAG_STRING
%state SVELTE_TAG
// Interpolation is handled separately (well, currently not at all)

%%
<YYINITIAL> {
  "<script" | "<style"  { yybegin(VERBATIM_HTML); return HTML_FRAGMENT; }
  "<"  { yybegin(HTML_TAG); return HTML_FRAGMENT; }
  "{" / \s*[#:/] { yybegin(SVELTE_TAG); return START_MUSTACHE; }
}

<SVELTE_TAG> {
  "#if"              { return IF; }
  "if"               { return ELSE_IF; }
  "/if"              { return END_IF; }

  "#each"            { return EACH; }
  "as"               { return AS; }
  ","                { return COMMA; }
  "("                { return START_PAREN; }
  ")"                { return END_PAREN; }
  "/each"            { return END_EACH; }

  "#await"           { return AWAIT; }
  "then"             { return AWAIT_THEN; }
  ":then"            { return THEN; }
  ":catch"           { return CATCH; }
  "/await"           { return AWAIT_END; }

  ":else"            { return ELSE; }
  {WHITE_SPACE}      { return WHITE_SPACE; }
  [^{}]              { return CODE_FRAGMENT; }

  "{"                {   leftBraceCount += 1; return CODE_FRAGMENT; }
  "}"                {
                          if (leftBraceCount == 0) {
                              yybegin(YYINITIAL);
                              return END_MUSTACHE;
                          } else {
                              leftBraceCount -= 1;
                              return CODE_FRAGMENT;
                          }
                     }
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

[^] { return HTML_FRAGMENT; }
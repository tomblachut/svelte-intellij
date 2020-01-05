package dev.blachut.svelte.lang.parsing.top;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static dev.blachut.svelte.lang.psi.SvelteTokenTypes.*;

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
%state VERBATIM_COMMENT
%state VERBATIM_HTML
%state HTML_TAG
%xstate ONLY_WHITESPACE
%xstate SINGLE_QUOTE
%xstate DOUBLE_QUOTE
%xstate TICKED_QUOTE

%%
<SVELTE_TAG> {
  {SINGLE_QUOTE}     { return beginQuote(SINGLE_QUOTE, CODE_FRAGMENT); }
  {DOUBLE_QUOTE}     { return beginQuote(DOUBLE_QUOTE, CODE_FRAGMENT); }
  {TICKED_QUOTE}     { return beginQuote(TICKED_QUOTE, CODE_FRAGMENT); }
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

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
  // as and then can work as Svelte keywords or JS identifiers depending on context
  private boolean rootKeywordsEnabled = false;

  private IElementType quotedToken;

  private Stack<Integer> stack = new Stack<>();

  private void pushState(int newState) {
    stack.push(yystate());
    yybegin(newState);
  }

  private void popState() {
    yybegin(stack.pop());
  }

  private void eatWsThenBegin(int nextState) {
      yybegin(nextState);
      pushState(ONLY_WHITESPACE);
  }

  private IElementType beginQuote(int quoteState, IElementType token) {
      quotedToken = token;
      pushState(quoteState);
      return quotedToken;
  }

  private void enableRootKeywords() {
      if (notNestedCode()) rootKeywordsEnabled = true;
  }

  private boolean notNestedCode() {
      return (bracesNestingLevel + parens + brackets) == 0;
  }

  private void resetCounters() {
      rootKeywordsEnabled = false;
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

%state SVELTE_TAG_START
%state SVELTE_TAG
%state SVELTE_TAG_PARAMETER
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
  "{"\s*"#"               { resetCounters(); yybegin(SVELTE_TAG_START); return START_OPENING_MUSTACHE; }
  "{"\s*":"               { resetCounters(); yybegin(SVELTE_TAG_START); return START_INNER_MUSTACHE; }
  "{"\s*"/"               { resetCounters(); yybegin(SVELTE_TAG_START); return START_CLOSING_MUSTACHE; }
  "{"                     { yybegin(SVELTE_INTERPOLATION_START); return START_MUSTACHE; }
}

<SVELTE_TAG_START> {
  "if"               { yybegin(SVELTE_TAG); return IF; }
  "else"             { eatWsThenBegin(SVELTE_TAG); return ELSE; }
  "each"             { yybegin(SVELTE_TAG); return EACH; }
  "await"            { yybegin(SVELTE_TAG); return AWAIT; }
  "then"             { yybegin(SVELTE_TAG_PARAMETER); return THEN; }
  "catch"            { yybegin(SVELTE_TAG_PARAMETER); return CATCH; }
  {ID}               { yybegin(SVELTE_TAG); return BAD_CHARACTER; }
  {WHITE_SPACE}      { return BAD_CHARACTER; }
}

<SVELTE_TAG> {
  "if"               { if (notNestedCode()) { return IF; } else { return CODE_FRAGMENT; } } // That could as well be always lexed as IF because if is an invalid token in JS expression
  "as"               { if (notNestedCode() && rootKeywordsEnabled) { yybegin(SVELTE_TAG_PARAMETER); return AS; } else { enableRootKeywords(); return CODE_FRAGMENT; } }
  "then"             { if (notNestedCode() && rootKeywordsEnabled) { yybegin(SVELTE_TAG_PARAMETER); return THEN; } else { enableRootKeywords(); return CODE_FRAGMENT; } }
  "("                { enableRootKeywords(); parens++; return CODE_FRAGMENT; }
  ")"                { parens--; return CODE_FRAGMENT; }
}

// Key expressions are wrapped in parens and can contain any number of paren pairs. Outermost parens need to be distinguished
<SVELTE_TAG_PARAMETER> {
  "("                { parens++; if (parens == 1) { return START_PAREN; } else { return CODE_FRAGMENT; } }
  ")"                { parens--; if (parens == 0) { return END_PAREN; } else { return CODE_FRAGMENT; } }
}

<SVELTE_TAG, SVELTE_TAG_PARAMETER> {
  {SINGLE_QUOTE}     { enableRootKeywords(); return beginQuote(SINGLE_QUOTE, CODE_FRAGMENT); }
  {DOUBLE_QUOTE}     { enableRootKeywords(); return beginQuote(DOUBLE_QUOTE, CODE_FRAGMENT); }
  {TICKED_QUOTE}     { enableRootKeywords(); return beginQuote(TICKED_QUOTE, CODE_FRAGMENT); }
  ","                { if (notNestedCode()) { return COMMA; } else { return CODE_FRAGMENT; } }
  "["                { enableRootKeywords(); brackets++; return CODE_FRAGMENT; }
  "]"                { brackets--; return CODE_FRAGMENT; }
  "{"                { enableRootKeywords(); bracesNestingLevel++; return CODE_FRAGMENT; }
  // Following eatWsThenBegin is a hack around formatter bugs
  "}"                { if (bracesNestingLevel == 0) { eatWsThenBegin(YYINITIAL); return END_MUSTACHE; } else { bracesNestingLevel--; return CODE_FRAGMENT; } }
  {WHITE_SPACE}/"}"  { if (bracesNestingLevel == 0) { return WHITE_SPACE; } else { return CODE_FRAGMENT; } }
  {WHITE_SPACE}      { return CODE_FRAGMENT; }
  {ID}               { enableRootKeywords(); return CODE_FRAGMENT; }
  [^]                { if (notNestedCode()) rootKeywordsEnabled = false; return CODE_FRAGMENT; }
}

<SVELTE_INTERPOLATION_START> {
  {WHITE_SPACE}/"@"  { return WHITE_SPACE; }
  "@html"            { yybegin(SVELTE_INTERPOLATION); return HTML_PREFIX; }
  "@debug"           { yybegin(SVELTE_INTERPOLATION); return DEBUG_PREFIX; }
  "@" | "@"{ID}      { yybegin(SVELTE_INTERPOLATION); return BAD_CHARACTER; }
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

<SVELTE_INTERPOLATION> {
  "{"                { bracesNestingLevel++; return CODE_FRAGMENT; }
  "}"                { if (bracesNestingLevel == 0) { eatWsThenBegin(YYINITIAL); return END_MUSTACHE; } else { bracesNestingLevel--; return CODE_FRAGMENT; } }
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

<ONLY_WHITESPACE> {
  {WHITE_SPACE}      { return WHITE_SPACE; }
  [^]                { popState(); yypushback(1); }
}

[^]                           { return HTML_FRAGMENT; }

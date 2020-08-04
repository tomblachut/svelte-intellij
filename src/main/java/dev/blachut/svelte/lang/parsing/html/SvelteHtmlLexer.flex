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
  private static int NO_QUOTE = 0;
  private static int SINGLE_QUOTE = 1;
  private static int DOUBLE_QUOTE = 2;
  private static int BACKQUOTE = 3;

  public int bracesNestingLevel = 0;
  public int quoteMode = NO_QUOTE;
  public int rawTag = 0;

  public _SvelteHtmlLexer() {
    this((java.io.Reader)null);
  }

  private void yybeginNestable(int state) {
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

%class _SvelteHtmlLexer
%public
%implements FlexLexer
%function advance
%type IElementType
//%debug

%state DOC_TYPE
%state COMMENT
%state START_TAG_NAME
%state END_TAG_NAME
%state BEFORE_TAG_ATTRIBUTES
%state TAG_ATTRIBUTES
%state ATTRIBUTE_BRACES
%state ATTRIBUTE_VALUE_START
%state ATTRIBUTE_VALUE_BRACES
%state ATTRIBUTE_VALUE_AFTER_BRACES
%state ATTRIBUTE_VALUE_DQ
%state ATTRIBUTE_VALUE_DQ_BRACES
%state ATTRIBUTE_VALUE_SQ
%state ATTRIBUTE_VALUE_SQ_BRACES
%state PROCESSING_INSTRUCTION
%state TAG_CHARACTERS
%state C_COMMENT_START
%state C_COMMENT_END
%state SVELTE_INTERPOLATION_START
%state SVELTE_INTERPOLATION_KEYWORD
%state SVELTE_INTERPOLATION
%state RAW_CONTENT
/* IMPORTANT! number of states should not exceed 16. See JspHighlightingLexer. */

ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+
WHITE_SPACE=\s+

SINGLE_QUOTE="'"
DOUBLE_QUOTE="\""
BACKQUOTE="`"
ESCAPED_QUOTES=\\'|\\\"|\\`

RAW_TAG_NAME=("script" | "style")
TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*
/* see http://www.w3.org/TR/html5/syntax.html#syntax-attribute-name */
ATTRIBUTE_NAME=[^ \n\r\t\f\"\'<>/={]([^ \n\r\t\f\"\'<>/=])*

DTD_REF= "\"" [^\"]* "\"" | "'" [^']* "'"
DOCTYPE= "<!" (D|d)(O|o)(C|c)(T|t)(Y|y)(P|p)(E|e)
HTML= (H|h)(T|t)(M|m)(L|l)
PUBLIC= (P|p)(U|u)(B|b)(L|l)(I|i)(C|c)
END_COMMENT="-->"

CONDITIONAL_COMMENT_CONDITION=({ALPHA})({ALPHA}|{WHITE_SPACE_CHARS}|{DIGIT}|"."|"("|")"|"|"|"!"|"&")*
%%

<YYINITIAL> "<?" { yybegin(PROCESSING_INSTRUCTION); return XmlTokenType.XML_PI_START; }
<PROCESSING_INSTRUCTION> "?"? ">" { yybegin(YYINITIAL); return XmlTokenType.XML_PI_END; }
<PROCESSING_INSTRUCTION> ([^\?\>] | (\?[^\>]))* { return XmlTokenType.XML_PI_TARGET; }

<YYINITIAL> {DOCTYPE} { yybegin(DOC_TYPE); return XmlTokenType.XML_DOCTYPE_START; }
<DOC_TYPE> {HTML} { return XmlTokenType.XML_NAME; }
<DOC_TYPE> {PUBLIC} { return XmlTokenType.XML_DOCTYPE_PUBLIC; }
<DOC_TYPE> {DTD_REF} { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
<DOC_TYPE> ">" { yybegin(YYINITIAL); return XmlTokenType.XML_DOCTYPE_END; }
<YYINITIAL> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_REAL_WHITE_SPACE; }
<DOC_TYPE,TAG_ATTRIBUTES,ATTRIBUTE_VALUE_START,PROCESSING_INSTRUCTION, START_TAG_NAME, END_TAG_NAME, TAG_CHARACTERS, RAW_CONTENT> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }
<YYINITIAL> "<" {TAG_NAME} { yybegin(START_TAG_NAME); yypushback(yylength()); }
<START_TAG_NAME, TAG_CHARACTERS> "<" { return XmlTokenType.XML_START_TAG_START; }

<RAW_CONTENT> "</" {RAW_TAG_NAME} { yybegin(END_TAG_NAME); yypushback(yylength()); }
<YYINITIAL> "</" {TAG_NAME} { yybegin(END_TAG_NAME); yypushback(yylength()); }
<YYINITIAL, END_TAG_NAME> "</" { return XmlTokenType.XML_END_TAG_START; }

<YYINITIAL> "<!--" { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_START; }
<COMMENT> "[" { yybegin(C_COMMENT_START); return XmlTokenType.XML_CONDITIONAL_COMMENT_START; }
<COMMENT> "<![" { yybegin(C_COMMENT_END); return XmlTokenType.XML_CONDITIONAL_COMMENT_END_START; }
<COMMENT> {END_COMMENT} | "<!-->" { yybegin(YYINITIAL); return XmlTokenType.XML_COMMENT_END; }
<COMMENT> "<!--" { return XmlTokenType.XML_BAD_CHARACTER; }
<COMMENT> "<!--->" | "--!>" { yybegin(YYINITIAL); return XmlTokenType.XML_BAD_CHARACTER; }
<COMMENT> ">" {
  // according to HTML spec (http://www.w3.org/html/wg/drafts/html/master/syntax.html#comments)
  // comments should start with <!-- and end with -->. The comment <!--> is not valid, but should terminate
  // comment token. Please note that it's not true for XML (http://www.w3.org/TR/REC-xml/#sec-comments)
  int loc = getTokenStart();
  char prev = zzBuffer.charAt(loc - 1);
  char prevPrev = zzBuffer.charAt(loc - 2);
  if (prev == '-' && prevPrev == '-') {
    yybegin(YYINITIAL); return XmlTokenType.XML_BAD_CHARACTER;
  }
  return XmlTokenType.XML_COMMENT_CHARACTERS;
}
<COMMENT> [^] { return XmlTokenType.XML_COMMENT_CHARACTERS; }

<C_COMMENT_START,C_COMMENT_END> {CONDITIONAL_COMMENT_CONDITION} { return XmlTokenType.XML_COMMENT_CHARACTERS; }
<C_COMMENT_START> [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }
<C_COMMENT_START> "]>" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_START_END; }
<C_COMMENT_START,C_COMMENT_END> {END_COMMENT} { yybegin(YYINITIAL); return XmlTokenType.XML_COMMENT_END; }
<C_COMMENT_END> "]" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_END; }
<C_COMMENT_END> [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }

<YYINITIAL> \\\$ {
  return XmlTokenType.XML_DATA_CHARACTERS;
}

<YYINITIAL> "{" { yybeginNestable(SVELTE_INTERPOLATION_START); return SvelteTokenTypes.START_MUSTACHE; }

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
  [^]                { yybegin(SVELTE_INTERPOLATION); yypushback(yylength()); }
}

<START_TAG_NAME> {RAW_TAG_NAME} { rawTag = 1; yybegin(BEFORE_TAG_ATTRIBUTES); return XmlTokenType.XML_NAME; }
<START_TAG_NAME, END_TAG_NAME> {TAG_NAME} { rawTag = 0; yybegin(BEFORE_TAG_ATTRIBUTES); return XmlTokenType.XML_NAME; }

<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, TAG_CHARACTERS, ATTRIBUTE_VALUE_START, ATTRIBUTE_VALUE_AFTER_BRACES> ">" {
  yybegin(rawTag == 1 ? RAW_CONTENT : YYINITIAL); return XmlTokenType.XML_TAG_END;
}
<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, TAG_CHARACTERS, ATTRIBUTE_VALUE_START, ATTRIBUTE_VALUE_AFTER_BRACES> "/>" {
  rawTag = 0; yybegin(YYINITIAL); return XmlTokenType.XML_EMPTY_ELEMENT_END;
}
<BEFORE_TAG_ATTRIBUTES> {WHITE_SPACE_CHARS} { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_WHITE_SPACE;}
<TAG_ATTRIBUTES> {ATTRIBUTE_NAME} { return XmlTokenType.XML_NAME; }
<TAG_ATTRIBUTES> "{" { yybeginNestable(ATTRIBUTE_BRACES); return SvelteTokenTypes.START_MUSTACHE; }
<TAG_ATTRIBUTES> "=" { yybegin(ATTRIBUTE_VALUE_START); return XmlTokenType.XML_EQ; }
<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, START_TAG_NAME, END_TAG_NAME> [^] { yybegin(YYINITIAL); yypushback(1); break; }

<TAG_CHARACTERS> [^] { return XmlTokenType.XML_TAG_CHARACTERS; }

<ATTRIBUTE_VALUE_START> [^ \n\r\t\f'\"\>{]([^ \n\r\t\f\>{]|(\/[^\>]))* { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
<ATTRIBUTE_VALUE_START> [^ \n\r\t\f'\"\>{]([^ \n\r\t\f\>{]|(\/[^\>]))* / "{" { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
<ATTRIBUTE_VALUE_START> "{" { yybeginNestable(ATTRIBUTE_VALUE_BRACES); return SvelteTokenTypes.START_MUSTACHE; }
<ATTRIBUTE_VALUE_START> "\"" { yybegin(ATTRIBUTE_VALUE_DQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }
<ATTRIBUTE_VALUE_START> "'" { yybegin(ATTRIBUTE_VALUE_SQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }

<ATTRIBUTE_VALUE_AFTER_BRACES> ([^ \n\r\t\f'\"\>{]|(\/[^\>]))+ { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
<ATTRIBUTE_VALUE_AFTER_BRACES> ([^ \n\r\t\f'\"\>{]|(\/[^\>]))+ / "{" { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
<ATTRIBUTE_VALUE_AFTER_BRACES> "{" { yybeginNestable(ATTRIBUTE_VALUE_BRACES); return SvelteTokenTypes.START_MUSTACHE; }
<ATTRIBUTE_VALUE_AFTER_BRACES> {WHITE_SPACE_CHARS} { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_WHITE_SPACE;}

<ATTRIBUTE_VALUE_DQ> {
  "\"" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  "{" { yybeginNestable(ATTRIBUTE_VALUE_DQ_BRACES); return SvelteTokenTypes.START_MUSTACHE; }
  \\\$ { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
  [^] { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<ATTRIBUTE_VALUE_SQ> {
  "'" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  "{" { yybeginNestable(ATTRIBUTE_VALUE_SQ_BRACES); return SvelteTokenTypes.START_MUSTACHE; }
  \\\$ { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
  [^] { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<ATTRIBUTE_VALUE_DQ_BRACES> "\"" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
<ATTRIBUTE_VALUE_SQ_BRACES> "'" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }

<SVELTE_INTERPOLATION, ATTRIBUTE_BRACES, ATTRIBUTE_VALUE_BRACES, ATTRIBUTE_VALUE_DQ_BRACES, ATTRIBUTE_VALUE_SQ_BRACES> {
  {ESCAPED_QUOTES}   { return SvelteTokenTypes.CODE_FRAGMENT; }
  {SINGLE_QUOTE}     { toggleQuoteMode(SINGLE_QUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  {DOUBLE_QUOTE}     { toggleQuoteMode(DOUBLE_QUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  {BACKQUOTE}        { toggleQuoteMode(BACKQUOTE); return SvelteTokenTypes.CODE_FRAGMENT; }
  "{"                { if (quoteMode == NO_QUOTE) { bracesNestingLevel++; } return SvelteTokenTypes.CODE_FRAGMENT; }
  "}"                {
          if (quoteMode != NO_QUOTE) { return SvelteTokenTypes.CODE_FRAGMENT; }
          if (bracesNestingLevel > 0) { bracesNestingLevel--; return SvelteTokenTypes.CODE_FRAGMENT; }

          if (yystate() == SVELTE_INTERPOLATION) yybegin(YYINITIAL);
          if (yystate() == ATTRIBUTE_BRACES) yybegin(TAG_ATTRIBUTES);
          if (yystate() == ATTRIBUTE_VALUE_BRACES) yybegin(ATTRIBUTE_VALUE_AFTER_BRACES);
          if (yystate() == ATTRIBUTE_VALUE_DQ_BRACES) yybegin(ATTRIBUTE_VALUE_DQ);
          if (yystate() == ATTRIBUTE_VALUE_SQ_BRACES) yybegin(ATTRIBUTE_VALUE_SQ);
          return SvelteTokenTypes.END_MUSTACHE;
                     }
  [^]                { return SvelteTokenTypes.CODE_FRAGMENT; }
}

"&lt;" |
"&gt;" |
"&apos;" |
"&quot;" |
"&nbsp;" |
"&amp;" |
"&#"{DIGIT}+";" |
"&#"[xX]({DIGIT}|[a-fA-F])+";" { return XmlTokenType.XML_CHAR_ENTITY_REF; }
"&"{TAG_NAME}";" { return XmlTokenType.XML_ENTITY_REF_TOKEN; }

<YYINITIAL> ([^<{&\$# \n\r\t\f]|(\\\$)|(\\#))* { return XmlTokenType.XML_DATA_CHARACTERS; }
<YYINITIAL, RAW_CONTENT> [^] { return XmlTokenType.XML_DATA_CHARACTERS; }
[^] { return XmlTokenType.XML_BAD_CHARACTER; }

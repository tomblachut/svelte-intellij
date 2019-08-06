package dev.blachut.svelte.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.javascript.JSTokenTypes.*;


%%

//%debug
%public
%class _SvelteScriptIdentifierLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state NEXT

%%

<YYINITIAL> "$"         { yybegin(NEXT); return DOLLAR; }
<YYINITIAL> "$$props"$  { return IDENTIFIER; }
[^]                     { return IDENTIFIER; }

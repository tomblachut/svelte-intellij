// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.blachut.svelte.lang.psi.impl.*;

public interface SvelteTypes {

  IElementType SCOPE = new SvelteElementType("SCOPE");

  IElementType AS = new SvelteElementType("as");
  IElementType AT = new SvelteElementType("AT");
  IElementType AWAIT = new SvelteElementType("await");
  IElementType CATCH = new SvelteElementType("catch");
  IElementType CODE_FRAGMENT = new SvelteElementType("<code>");
  IElementType COLON = new SvelteElementType("COLON");
  IElementType COMMA = new SvelteElementType(",");
  IElementType DEBUG_PREFIX = new SvelteElementType("@debug");
  IElementType EACH = new SvelteElementType("each");
  IElementType ELSE = new SvelteElementType("else");
  IElementType END_MUSTACHE = new SvelteElementType("}");
  IElementType END_PAREN = new SvelteElementType(")");
  IElementType HASH = new SvelteElementType("HASH");
  IElementType HTML_FRAGMENT = new SvelteElementType("<markup>");
  IElementType HTML_PREFIX = new SvelteElementType("@html");
  IElementType IF = new SvelteElementType("if");
  IElementType LAZY_AWAIT = new SvelteElementType("LAZY_AWAIT");
  IElementType LAZY_CATCH = new SvelteElementType("LAZY_CATCH");
  IElementType LAZY_EACH = new SvelteElementType("LAZY_EACH");
  IElementType LAZY_ELSE = new SvelteElementType("LAZY_ELSE");
  IElementType LAZY_IF = new SvelteElementType("LAZY_IF");
  IElementType LAZY_THEN = new SvelteElementType("LAZY_THEN");
  IElementType SLASH = new SvelteElementType("SLASH");
  IElementType START_CLOSING_MUSTACHE = new SvelteElementType("{/");
  IElementType START_INNER_MUSTACHE = new SvelteElementType("{:");
  IElementType START_MUSTACHE = new SvelteElementType("{");
  IElementType START_OPENING_MUSTACHE = new SvelteElementType("{#");
  IElementType START_PAREN = new SvelteElementType("(");
  IElementType TEMP_PREFIX = new SvelteElementType("TEMP_PREFIX");
  IElementType THEN = new SvelteElementType("then");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == SCOPE) {
        return new SvelteScopeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.blachut.svelte.lang.psi.impl.*;

public interface SvelteTypes {

  IElementType AWAIT_BLOCK = new SvelteElementType("AWAIT_BLOCK");
  IElementType AWAIT_BLOCK_CLOSING = new SvelteElementType("AWAIT_BLOCK_CLOSING");
  IElementType AWAIT_BLOCK_OPENING = new SvelteElementType("AWAIT_BLOCK_OPENING");
  IElementType AWAIT_THEN_BLOCK_OPENING = new SvelteElementType("AWAIT_THEN_BLOCK_OPENING");
  IElementType CATCH_CONTINUATION = new SvelteElementType("CATCH_CONTINUATION");
  IElementType EACH_BLOCK = new SvelteElementType("EACH_BLOCK");
  IElementType EACH_BLOCK_CLOSING = new SvelteElementType("EACH_BLOCK_CLOSING");
  IElementType EACH_BLOCK_OPENING = new SvelteElementType("EACH_BLOCK_OPENING");
  IElementType ELSE_CONTINUATION = new SvelteElementType("ELSE_CONTINUATION");
  IElementType EXPRESSION = new SvelteElementType("EXPRESSION");
  IElementType IF_BLOCK = new SvelteElementType("IF_BLOCK");
  IElementType IF_BLOCK_CLOSING = new SvelteElementType("IF_BLOCK_CLOSING");
  IElementType IF_BLOCK_OPENING = new SvelteElementType("IF_BLOCK_OPENING");
  IElementType IF_ELSE_CONTINUATION = new SvelteElementType("IF_ELSE_CONTINUATION");
  IElementType PARAMETER = new SvelteElementType("PARAMETER");
  IElementType SCOPE = new SvelteElementType("SCOPE");
  IElementType THEN_CONTINUATION = new SvelteElementType("THEN_CONTINUATION");

  IElementType AS = new SvelteElementType("as");
  IElementType AWAIT = new SvelteElementType("#await");
  IElementType AWAIT_END = new SvelteElementType("/await");
  IElementType AWAIT_THEN = new SvelteElementType("then");
  IElementType CATCH = new SvelteElementType(":catch");
  IElementType CODE_FRAGMENT = new SvelteElementType("CODE_FRAGMENT");
  IElementType COMMA = new SvelteElementType(",");
  IElementType EACH = new SvelteElementType("#each");
  IElementType ELSE = new SvelteElementType(":else");
  IElementType ELSE_IF = new SvelteElementType("if");
  IElementType END_EACH = new SvelteElementType("/each");
  IElementType END_IF = new SvelteElementType("/if");
  IElementType END_MUSTACHE = new SvelteElementType("}");
  IElementType END_PAREN = new SvelteElementType(")");
  IElementType HTML_FRAGMENT = new SvelteElementType("HTML_FRAGMENT");
  IElementType IF = new SvelteElementType("#if");
  IElementType START_MUSTACHE = new SvelteElementType("{");
  IElementType START_PAREN = new SvelteElementType("(");
  IElementType THEN = new SvelteElementType(":then");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AWAIT_BLOCK) {
        return new SvelteAwaitBlockImpl(node);
      }
      else if (type == AWAIT_BLOCK_CLOSING) {
        return new SvelteAwaitBlockClosingImpl(node);
      }
      else if (type == AWAIT_BLOCK_OPENING) {
        return new SvelteAwaitBlockOpeningImpl(node);
      }
      else if (type == AWAIT_THEN_BLOCK_OPENING) {
        return new SvelteAwaitThenBlockOpeningImpl(node);
      }
      else if (type == CATCH_CONTINUATION) {
        return new SvelteCatchContinuationImpl(node);
      }
      else if (type == EACH_BLOCK) {
        return new SvelteEachBlockImpl(node);
      }
      else if (type == EACH_BLOCK_CLOSING) {
        return new SvelteEachBlockClosingImpl(node);
      }
      else if (type == EACH_BLOCK_OPENING) {
        return new SvelteEachBlockOpeningImpl(node);
      }
      else if (type == ELSE_CONTINUATION) {
        return new SvelteElseContinuationImpl(node);
      }
      else if (type == EXPRESSION) {
        return new SvelteExpressionImpl(node);
      }
      else if (type == IF_BLOCK) {
        return new SvelteIfBlockImpl(node);
      }
      else if (type == IF_BLOCK_CLOSING) {
        return new SvelteIfBlockClosingImpl(node);
      }
      else if (type == IF_BLOCK_OPENING) {
        return new SvelteIfBlockOpeningImpl(node);
      }
      else if (type == IF_ELSE_CONTINUATION) {
        return new SvelteIfElseContinuationImpl(node);
      }
      else if (type == PARAMETER) {
        return new SvelteParameterImpl(node);
      }
      else if (type == SCOPE) {
        return new SvelteScopeImpl(node);
      }
      else if (type == THEN_CONTINUATION) {
        return new SvelteThenContinuationImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.blachut.svelte.lang.psi.impl.*;

public interface SvelteTypes {

  IElementType AWAIT_BLOCK = new SvelteElementType("AWAIT_BLOCK");
  IElementType AWAIT_BLOCK_CLOSING_TAG = new SvelteElementType("AWAIT_BLOCK_CLOSING_TAG");
  IElementType AWAIT_BLOCK_OPENING = new SvelteElementType("AWAIT_BLOCK_OPENING");
  IElementType AWAIT_BLOCK_OPENING_TAG = new SvelteElementType("AWAIT_BLOCK_OPENING_TAG");
  IElementType AWAIT_THEN_BLOCK_OPENING = new SvelteElementType("AWAIT_THEN_BLOCK_OPENING");
  IElementType AWAIT_THEN_BLOCK_OPENING_TAG = new SvelteElementType("AWAIT_THEN_BLOCK_OPENING_TAG");
  IElementType BLOCK = new SvelteElementType("BLOCK");
  IElementType CATCH_CONTINUATION = new SvelteElementType("CATCH_CONTINUATION");
  IElementType CATCH_CONTINUATION_TAG = new SvelteElementType("CATCH_CONTINUATION_TAG");
  IElementType EACH_BLOCK = new SvelteElementType("EACH_BLOCK");
  IElementType EACH_BLOCK_CLOSING_TAG = new SvelteElementType("EACH_BLOCK_CLOSING_TAG");
  IElementType EACH_BLOCK_OPENING = new SvelteElementType("EACH_BLOCK_OPENING");
  IElementType EACH_BLOCK_OPENING_TAG = new SvelteElementType("EACH_BLOCK_OPENING_TAG");
  IElementType ELSE_CONTINUATION = new SvelteElementType("ELSE_CONTINUATION");
  IElementType ELSE_CONTINUATION_TAG = new SvelteElementType("ELSE_CONTINUATION_TAG");
  IElementType ELSE_IF_CONTINUATION = new SvelteElementType("ELSE_IF_CONTINUATION");
  IElementType ELSE_IF_CONTINUATION_TAG = new SvelteElementType("ELSE_IF_CONTINUATION_TAG");
  IElementType EXPRESSION = new SvelteElementType("EXPRESSION");
  IElementType IF_BLOCK = new SvelteElementType("IF_BLOCK");
  IElementType IF_BLOCK_CLOSING_TAG = new SvelteElementType("IF_BLOCK_CLOSING_TAG");
  IElementType IF_BLOCK_OPENING = new SvelteElementType("IF_BLOCK_OPENING");
  IElementType IF_BLOCK_OPENING_TAG = new SvelteElementType("IF_BLOCK_OPENING_TAG");
  IElementType INTERPOLATION = new SvelteElementType("INTERPOLATION");
  IElementType PARAMETER = new SvelteElementType("PARAMETER");
  IElementType SCOPE = new SvelteElementType("SCOPE");
  IElementType THEN_CONTINUATION = new SvelteElementType("THEN_CONTINUATION");
  IElementType THEN_CONTINUATION_TAG = new SvelteElementType("THEN_CONTINUATION_TAG");

  IElementType AS = new SvelteElementType("as");
  IElementType AWAIT = new SvelteElementType("await");
  IElementType CATCH = new SvelteElementType("catch");
  IElementType CODE_FRAGMENT = new SvelteElementType("CODE_FRAGMENT");
  IElementType COMMA = new SvelteElementType(",");
  IElementType DEBUG_PREFIX = new SvelteElementType("@debug");
  IElementType EACH = new SvelteElementType("each");
  IElementType ELSE = new SvelteElementType("else");
  IElementType END_MUSTACHE = new SvelteElementType("}");
  IElementType END_PAREN = new SvelteElementType(")");
  IElementType HTML_FRAGMENT = new SvelteElementType("<markup>");
  IElementType HTML_PREFIX = new SvelteElementType("@html");
  IElementType IF = new SvelteElementType("if");
  IElementType START_CLOSING_MUSTACHE = new SvelteElementType("{/");
  IElementType START_INNER_MUSTACHE = new SvelteElementType("{:");
  IElementType START_MUSTACHE = new SvelteElementType("{");
  IElementType START_OPENING_MUSTACHE = new SvelteElementType("{#");
  IElementType START_PAREN = new SvelteElementType("(");
  IElementType THEN = new SvelteElementType("then");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AWAIT_BLOCK) {
        return new SvelteAwaitBlockImpl(node);
      }
      else if (type == AWAIT_BLOCK_CLOSING_TAG) {
        return new SvelteAwaitBlockClosingTagImpl(node);
      }
      else if (type == AWAIT_BLOCK_OPENING) {
        return new SvelteAwaitBlockOpeningImpl(node);
      }
      else if (type == AWAIT_BLOCK_OPENING_TAG) {
        return new SvelteAwaitBlockOpeningTagImpl(node);
      }
      else if (type == AWAIT_THEN_BLOCK_OPENING) {
        return new SvelteAwaitThenBlockOpeningImpl(node);
      }
      else if (type == AWAIT_THEN_BLOCK_OPENING_TAG) {
        return new SvelteAwaitThenBlockOpeningTagImpl(node);
      }
      else if (type == CATCH_CONTINUATION) {
        return new SvelteCatchContinuationImpl(node);
      }
      else if (type == CATCH_CONTINUATION_TAG) {
        return new SvelteCatchContinuationTagImpl(node);
      }
      else if (type == EACH_BLOCK) {
        return new SvelteEachBlockImpl(node);
      }
      else if (type == EACH_BLOCK_CLOSING_TAG) {
        return new SvelteEachBlockClosingTagImpl(node);
      }
      else if (type == EACH_BLOCK_OPENING) {
        return new SvelteEachBlockOpeningImpl(node);
      }
      else if (type == EACH_BLOCK_OPENING_TAG) {
        return new SvelteEachBlockOpeningTagImpl(node);
      }
      else if (type == ELSE_CONTINUATION) {
        return new SvelteElseContinuationImpl(node);
      }
      else if (type == ELSE_CONTINUATION_TAG) {
        return new SvelteElseContinuationTagImpl(node);
      }
      else if (type == ELSE_IF_CONTINUATION) {
        return new SvelteElseIfContinuationImpl(node);
      }
      else if (type == ELSE_IF_CONTINUATION_TAG) {
        return new SvelteElseIfContinuationTagImpl(node);
      }
      else if (type == EXPRESSION) {
        return new SvelteExpressionImpl(node);
      }
      else if (type == IF_BLOCK) {
        return new SvelteIfBlockImpl(node);
      }
      else if (type == IF_BLOCK_CLOSING_TAG) {
        return new SvelteIfBlockClosingTagImpl(node);
      }
      else if (type == IF_BLOCK_OPENING) {
        return new SvelteIfBlockOpeningImpl(node);
      }
      else if (type == IF_BLOCK_OPENING_TAG) {
        return new SvelteIfBlockOpeningTagImpl(node);
      }
      else if (type == INTERPOLATION) {
        return new SvelteInterpolationImpl(node);
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
      else if (type == THEN_CONTINUATION_TAG) {
        return new SvelteThenContinuationTagImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

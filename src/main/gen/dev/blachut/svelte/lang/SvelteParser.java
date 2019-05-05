// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class SvelteParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t instanceof IFileElementType) {
      r = parse_root_(t, b, 0);
    }
    else {
      r = false;
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return svelteComponent(b, l + 1);
  }

  /* ********************************************************** */
  // (awaitThenBlockOpening | awaitBlockOpening thenContinuation) (catchContinuation)? awaitBlockClosing
  public static boolean awaitBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_BLOCK, null);
    r = awaitBlock_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, awaitBlock_1(b, l + 1));
    r = p && awaitBlockClosing(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // awaitThenBlockOpening | awaitBlockOpening thenContinuation
  private static boolean awaitBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitThenBlockOpening(b, l + 1);
    if (!r) r = awaitBlock_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // awaitBlockOpening thenContinuation
  private static boolean awaitBlock_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitBlockOpening(b, l + 1);
    r = r && thenContinuation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (catchContinuation)?
  private static boolean awaitBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_1")) return false;
    awaitBlock_1_0(b, l + 1);
    return true;
  }

  // (catchContinuation)
  private static boolean awaitBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = catchContinuation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' '/await' '}'
  public static boolean awaitBlockClosing(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlockClosing")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_BLOCK_CLOSING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, AWAIT_END, END_MUSTACHE);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // awaitBlockOpeningTag scope
  public static boolean awaitBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitBlockOpeningTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, AWAIT_BLOCK_OPENING, r);
    return r;
  }

  /* ********************************************************** */
  // '{' '#await' expression '}'
  public static boolean awaitBlockOpeningTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlockOpeningTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_BLOCK_OPENING_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, AWAIT);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // awaitThenBlockOpeningTag scope
  public static boolean awaitThenBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitThenBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitThenBlockOpeningTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, AWAIT_THEN_BLOCK_OPENING, r);
    return r;
  }

  /* ********************************************************** */
  // '{' '#await' expression 'then' parameter '}'
  public static boolean awaitThenBlockOpeningTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitThenBlockOpeningTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_THEN_BLOCK_OPENING_TAG, null);
    r = consumeTokens(b, 0, START_MUSTACHE, AWAIT);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, AWAIT_THEN);
    p = r; // pin = 4
    r = r && report_error_(b, parameter(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ifBlock | eachBlock | awaitBlock
  static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    r = ifBlock(b, l + 1);
    if (!r) r = eachBlock(b, l + 1);
    if (!r) r = awaitBlock(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // catchContinuationTag scope
  public static boolean catchContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = catchContinuationTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, CATCH_CONTINUATION, r);
    return r;
  }

  /* ********************************************************** */
  // '{' ':catch' parameter '}'
  public static boolean catchContinuationTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchContinuationTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CONTINUATION_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, CATCH);
    p = r; // pin = 2
    r = r && report_error_(b, parameter(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // eachBlockOpening elseContinuation? eachBlockClosing
  public static boolean eachBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlock")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EACH_BLOCK, null);
    r = eachBlockOpening(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, eachBlock_1(b, l + 1));
    r = p && eachBlockClosing(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // elseContinuation?
  private static boolean eachBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlock_1")) return false;
    elseContinuation(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '{' '/each' '}'
  public static boolean eachBlockClosing(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockClosing")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EACH_BLOCK_CLOSING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, END_EACH, END_MUSTACHE);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // eachBlockOpeningTag scope
  public static boolean eachBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eachBlockOpeningTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, EACH_BLOCK_OPENING, r);
    return r;
  }

  /* ********************************************************** */
  // '{' '#each' expression 'as' parameter (',' parameter)? ('(' expression ')')? '}'
  public static boolean eachBlockOpeningTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpeningTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EACH_BLOCK_OPENING_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, EACH);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && report_error_(b, consumeToken(b, AS)) && r;
    r = p && report_error_(b, parameter(b, l + 1)) && r;
    r = p && report_error_(b, eachBlockOpeningTag_5(b, l + 1)) && r;
    r = p && report_error_(b, eachBlockOpeningTag_6(b, l + 1)) && r;
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' parameter)?
  private static boolean eachBlockOpeningTag_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpeningTag_5")) return false;
    eachBlockOpeningTag_5_0(b, l + 1);
    return true;
  }

  // ',' parameter
  private static boolean eachBlockOpeningTag_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpeningTag_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('(' expression ')')?
  private static boolean eachBlockOpeningTag_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpeningTag_6")) return false;
    eachBlockOpeningTag_6_0(b, l + 1);
    return true;
  }

  // '(' expression ')'
  private static boolean eachBlockOpeningTag_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpeningTag_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, START_PAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, END_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // elseContinuationTag scope
  public static boolean elseContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseContinuationTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, ELSE_CONTINUATION, r);
    return r;
  }

  /* ********************************************************** */
  // '{' ':else' '}'
  public static boolean elseContinuationTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseContinuationTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_CONTINUATION_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, ELSE, END_MUSTACHE);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // elseIfContinuationTag scope
  public static boolean elseIfContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseIfContinuationTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, ELSE_IF_CONTINUATION, r);
    return r;
  }

  /* ********************************************************** */
  // '{' ':else' 'if' expression '}'
  public static boolean elseIfContinuationTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfContinuationTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_IF_CONTINUATION_TAG, null);
    r = consumeTokens(b, 3, START_MUSTACHE, ELSE, ELSE_IF);
    p = r; // pin = 3
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CODE_FRAGMENT
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    if (!nextTokenIs(b, "<expression>", CODE_FRAGMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = consumeToken(b, CODE_FRAGMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ifBlockOpening elseIfContinuation* elseContinuation? ifBlockClosing
  public static boolean ifBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BLOCK, null);
    r = ifBlockOpening(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, ifBlock_1(b, l + 1));
    r = p && report_error_(b, ifBlock_2(b, l + 1)) && r;
    r = p && ifBlockClosing(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // elseIfContinuation*
  private static boolean ifBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!elseIfContinuation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifBlock_1", c)) break;
    }
    return true;
  }

  // elseContinuation?
  private static boolean ifBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_2")) return false;
    elseContinuation(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '{' '/if' '}'
  public static boolean ifBlockClosing(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlockClosing")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BLOCK_CLOSING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, END_IF, END_MUSTACHE);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ifBlockOpeningTag scope
  public static boolean ifBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifBlockOpeningTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, IF_BLOCK_OPENING, r);
    return r;
  }

  /* ********************************************************** */
  // '{' '#if' expression '}'
  public static boolean ifBlockOpeningTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlockOpeningTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BLOCK_OPENING_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, IF);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '{' expression '}'
  public static boolean interpolation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interpolation")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INTERPOLATION, "<interpolation>");
    r = consumeToken(b, START_MUSTACHE);
    r = r && expression(b, l + 1);
    p = r; // pin = 2
    r = r && consumeToken(b, END_MUSTACHE);
    exit_section_(b, l, m, r, p, interpolation_recover_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // &'}'
  static boolean interpolation_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interpolation_recover")) return false;
    if (!nextTokenIs(b, END_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, END_MUSTACHE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CODE_FRAGMENT
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, "<parameter>", CODE_FRAGMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = consumeToken(b, CODE_FRAGMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (block|interpolation|HTML_FRAGMENT)*
  public static boolean scope(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope")) return false;
    Marker m = enter_section_(b, l, _NONE_, SCOPE, "<scope>");
    while (true) {
      int c = current_position_(b);
      if (!scope_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scope", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // block|interpolation|HTML_FRAGMENT
  private static boolean scope_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = block(b, l + 1);
    if (!r) r = interpolation(b, l + 1);
    if (!r) r = consumeToken(b, HTML_FRAGMENT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // scope
  static boolean svelteComponent(PsiBuilder b, int l) {
    return scope(b, l + 1);
  }

  /* ********************************************************** */
  // thenContinuationTag scope
  public static boolean thenContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = thenContinuationTag(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, THEN_CONTINUATION, r);
    return r;
  }

  /* ********************************************************** */
  // '{' ':then' parameter '}'
  public static boolean thenContinuationTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenContinuationTag")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, THEN_CONTINUATION_TAG, null);
    r = consumeTokens(b, 2, START_MUSTACHE, THEN);
    p = r; // pin = 2
    r = r && report_error_(b, parameter(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  static final Parser interpolation_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return interpolation_recover(b, l + 1);
    }
  };
}

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
  // (awaitThenBlockOpening scope | awaitBlockOpening scope thenContinuation scope) (catchContinuation scope)? awaitBlockClosing
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

  // awaitThenBlockOpening scope | awaitBlockOpening scope thenContinuation scope
  private static boolean awaitBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitBlock_0_0(b, l + 1);
    if (!r) r = awaitBlock_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // awaitThenBlockOpening scope
  private static boolean awaitBlock_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitThenBlockOpening(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // awaitBlockOpening scope thenContinuation scope
  private static boolean awaitBlock_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = awaitBlockOpening(b, l + 1);
    r = r && scope(b, l + 1);
    r = r && thenContinuation(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (catchContinuation scope)?
  private static boolean awaitBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_1")) return false;
    awaitBlock_1_0(b, l + 1);
    return true;
  }

  // catchContinuation scope
  private static boolean awaitBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = catchContinuation(b, l + 1);
    r = r && scope(b, l + 1);
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
  // '{' '#await' expression '}'
  public static boolean awaitBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_BLOCK_OPENING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, AWAIT);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '{' '#await' expression 'then' parameter '}'
  public static boolean awaitThenBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitThenBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_THEN_BLOCK_OPENING, null);
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
  // '{' ':catch' parameter '}'
  public static boolean catchContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CONTINUATION, null);
    r = consumeTokens(b, 2, START_MUSTACHE, CATCH);
    p = r; // pin = 2
    r = r && report_error_(b, parameter(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // eachBlockOpening scope (elseContinuation scope)? eachBlockClosing
  public static boolean eachBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlock")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EACH_BLOCK, null);
    r = eachBlockOpening(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, scope(b, l + 1));
    r = p && report_error_(b, eachBlock_2(b, l + 1)) && r;
    r = p && eachBlockClosing(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (elseContinuation scope)?
  private static boolean eachBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlock_2")) return false;
    eachBlock_2_0(b, l + 1);
    return true;
  }

  // elseContinuation scope
  private static boolean eachBlock_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlock_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseContinuation(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // '{' '#each' expression 'as' parameter (',' parameter)? ('(' expression ')')? '}'
  public static boolean eachBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EACH_BLOCK_OPENING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, EACH);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && report_error_(b, consumeToken(b, AS)) && r;
    r = p && report_error_(b, parameter(b, l + 1)) && r;
    r = p && report_error_(b, eachBlockOpening_5(b, l + 1)) && r;
    r = p && report_error_(b, eachBlockOpening_6(b, l + 1)) && r;
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' parameter)?
  private static boolean eachBlockOpening_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening_5")) return false;
    eachBlockOpening_5_0(b, l + 1);
    return true;
  }

  // ',' parameter
  private static boolean eachBlockOpening_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('(' expression ')')?
  private static boolean eachBlockOpening_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening_6")) return false;
    eachBlockOpening_6_0(b, l + 1);
    return true;
  }

  // '(' expression ')'
  private static boolean eachBlockOpening_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eachBlockOpening_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, START_PAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, END_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' ':else' '}'
  public static boolean elseContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_CONTINUATION, null);
    r = consumeTokens(b, 2, START_MUSTACHE, ELSE, END_MUSTACHE);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CODE_FRAGMENT
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    if (!nextTokenIs(b, CODE_FRAGMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CODE_FRAGMENT);
    exit_section_(b, m, EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // ifBlockOpening scope (ifElseContinuation scope)* (elseContinuation scope)? ifBlockClosing
  public static boolean ifBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BLOCK, null);
    r = ifBlockOpening(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, scope(b, l + 1));
    r = p && report_error_(b, ifBlock_2(b, l + 1)) && r;
    r = p && report_error_(b, ifBlock_3(b, l + 1)) && r;
    r = p && ifBlockClosing(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (ifElseContinuation scope)*
  private static boolean ifBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifBlock_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifBlock_2", c)) break;
    }
    return true;
  }

  // ifElseContinuation scope
  private static boolean ifBlock_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifElseContinuation(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (elseContinuation scope)?
  private static boolean ifBlock_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_3")) return false;
    ifBlock_3_0(b, l + 1);
    return true;
  }

  // elseContinuation scope
  private static boolean ifBlock_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlock_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseContinuation(b, l + 1);
    r = r && scope(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // '{' '#if' expression '}'
  public static boolean ifBlockOpening(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifBlockOpening")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BLOCK_OPENING, null);
    r = consumeTokens(b, 2, START_MUSTACHE, IF);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, END_MUSTACHE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '{' ':else' 'if' expression '}'
  public static boolean ifElseContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_ELSE_CONTINUATION, null);
    r = consumeTokens(b, 3, START_MUSTACHE, ELSE, ELSE_IF);
    p = r; // pin = 3
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
    if (!nextTokenIs(b, CODE_FRAGMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CODE_FRAGMENT);
    exit_section_(b, m, PARAMETER, r);
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
  // '{' ':then' parameter '}'
  public static boolean thenContinuation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenContinuation")) return false;
    if (!nextTokenIs(b, START_MUSTACHE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, THEN_CONTINUATION, null);
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

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

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, EXTENDS_SETS_);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    if (type instanceof IFileElementType) {
      result = parse_root_(type, builder, 0);
    }
    else {
      result = false;
    }
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    return svelteComponent(builder, level + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(AWAIT_BLOCK_CLOSING_TAG, EACH_BLOCK_CLOSING_TAG, IF_BLOCK_CLOSING_TAG),
    create_token_set_(AWAIT_BLOCK, BLOCK, EACH_BLOCK, IF_BLOCK),
    create_token_set_(AWAIT_BLOCK_OPENING_TAG, AWAIT_THEN_BLOCK_OPENING_TAG, EACH_BLOCK_OPENING_TAG, IF_BLOCK_OPENING_TAG),
    create_token_set_(CATCH_CONTINUATION_TAG, ELSE_CONTINUATION_TAG, ELSE_IF_CONTINUATION_TAG, THEN_CONTINUATION_TAG),
  };

  /* ********************************************************** */
  // awaitBlockOpening thenContinuation
  static boolean awaitAndThen(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitAndThen")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = awaitBlockOpening(builder, level + 1);
    pinned = result; // pin = 1
    result = result && thenContinuation(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // (awaitThenBlockOpening | awaitAndThen) (catchContinuation)? awaitBlockClosingTag
  public static boolean awaitBlock(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlock")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, AWAIT_BLOCK, null);
    result = awaitBlock_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, awaitBlock_1(builder, level + 1));
    result = pinned && awaitBlockClosingTag(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // awaitThenBlockOpening | awaitAndThen
  private static boolean awaitBlock_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlock_0")) return false;
    boolean result;
    result = awaitThenBlockOpening(builder, level + 1);
    if (!result) result = awaitAndThen(builder, level + 1);
    return result;
  }

  // (catchContinuation)?
  private static boolean awaitBlock_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlock_1")) return false;
    awaitBlock_1_0(builder, level + 1);
    return true;
  }

  // (catchContinuation)
  private static boolean awaitBlock_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlock_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = catchContinuation(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '{/' 'await' '}'
  public static boolean awaitBlockClosingTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlockClosingTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, AWAIT_BLOCK_CLOSING_TAG, "<await block closing tag>");
    result = consumeTokens(builder, 2, START_CLOSING_MUSTACHE, AWAIT, END_MUSTACHE);
    pinned = result; // pin = 2
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // awaitBlockOpeningTag scope
  public static boolean awaitBlockOpening(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlockOpening")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = awaitBlockOpeningTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, marker, AWAIT_BLOCK_OPENING, result);
    return result;
  }

  /* ********************************************************** */
  // '{#' 'await' expression '}'
  public static boolean awaitBlockOpeningTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitBlockOpeningTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, AWAIT_BLOCK_OPENING_TAG, "<await block opening tag>");
    result = consumeTokens(builder, 2, START_OPENING_MUSTACHE, AWAIT);
    pinned = result; // pin = 2
    result = result && report_error_(builder, expression(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // awaitThenBlockOpeningTag scope
  public static boolean awaitThenBlockOpening(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitThenBlockOpening")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = awaitThenBlockOpeningTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, marker, AWAIT_THEN_BLOCK_OPENING, result);
    return result;
  }

  /* ********************************************************** */
  // '{#' 'await' expression 'then' parameter '}'
  public static boolean awaitThenBlockOpeningTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "awaitThenBlockOpeningTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, AWAIT_THEN_BLOCK_OPENING_TAG, "<await then block opening tag>");
    result = consumeTokens(builder, 0, START_OPENING_MUSTACHE, AWAIT);
    result = result && expression(builder, level + 1);
    result = result && consumeToken(builder, THEN);
    pinned = result; // pin = 4
    result = result && report_error_(builder, parameter(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // ifBlock | eachBlock | awaitBlock
  public static boolean block(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "block")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, BLOCK, null);
    result = ifBlock(builder, level + 1);
    if (!result) result = eachBlock(builder, level + 1);
    if (!result) result = awaitBlock(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // catchContinuationTag scope
  public static boolean catchContinuation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "catchContinuation")) return false;
    if (!nextTokenIs(builder, "<catch block>", START_INNER_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, CATCH_CONTINUATION, "<catch block>");
    result = catchContinuationTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '{:' 'catch' parameter '}'
  public static boolean catchContinuationTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "catchContinuationTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, CATCH_CONTINUATION_TAG, "<catch continuation tag>");
    result = consumeTokens(builder, 2, START_INNER_MUSTACHE, CATCH);
    pinned = result; // pin = 2
    result = result && report_error_(builder, parameter(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // eachBlockOpening elseContinuation? eachBlockClosingTag
  public static boolean eachBlock(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlock")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EACH_BLOCK, null);
    result = eachBlockOpening(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, eachBlock_1(builder, level + 1));
    result = pinned && eachBlockClosingTag(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // elseContinuation?
  private static boolean eachBlock_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlock_1")) return false;
    elseContinuation(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '{/' 'each' '}'
  public static boolean eachBlockClosingTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockClosingTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EACH_BLOCK_CLOSING_TAG, "<each block closing tag>");
    result = consumeTokens(builder, 2, START_CLOSING_MUSTACHE, EACH, END_MUSTACHE);
    pinned = result; // pin = 2
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // eachBlockOpeningTag scope
  public static boolean eachBlockOpening(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpening")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = eachBlockOpeningTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, marker, EACH_BLOCK_OPENING, result);
    return result;
  }

  /* ********************************************************** */
  // '{#' 'each' expression 'as' parameter (',' parameter)? ('(' expression ')')? '}'
  public static boolean eachBlockOpeningTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpeningTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EACH_BLOCK_OPENING_TAG, "<each block opening tag>");
    result = consumeTokens(builder, 2, START_OPENING_MUSTACHE, EACH);
    pinned = result; // pin = 2
    result = result && report_error_(builder, expression(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, AS)) && result;
    result = pinned && report_error_(builder, parameter(builder, level + 1)) && result;
    result = pinned && report_error_(builder, eachBlockOpeningTag_5(builder, level + 1)) && result;
    result = pinned && report_error_(builder, eachBlockOpeningTag_6(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  // (',' parameter)?
  private static boolean eachBlockOpeningTag_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpeningTag_5")) return false;
    eachBlockOpeningTag_5_0(builder, level + 1);
    return true;
  }

  // ',' parameter
  private static boolean eachBlockOpeningTag_5_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpeningTag_5_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, COMMA);
    result = result && parameter(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // ('(' expression ')')?
  private static boolean eachBlockOpeningTag_6(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpeningTag_6")) return false;
    eachBlockOpeningTag_6_0(builder, level + 1);
    return true;
  }

  // '(' expression ')'
  private static boolean eachBlockOpeningTag_6_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "eachBlockOpeningTag_6_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, START_PAREN);
    result = result && expression(builder, level + 1);
    result = result && consumeToken(builder, END_PAREN);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // elseContinuationTag scope
  public static boolean elseContinuation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "elseContinuation")) return false;
    if (!nextTokenIs(builder, "<else block>", START_INNER_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ELSE_CONTINUATION, "<else block>");
    result = elseContinuationTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '{:' 'else' '}'
  public static boolean elseContinuationTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "elseContinuationTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ELSE_CONTINUATION_TAG, "<else continuation tag>");
    result = consumeTokens(builder, 2, START_INNER_MUSTACHE, ELSE, END_MUSTACHE);
    pinned = result; // pin = 2
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // elseIfContinuationTag scope
  public static boolean elseIfContinuation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "elseIfContinuation")) return false;
    if (!nextTokenIs(builder, "<else if block>", START_INNER_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ELSE_IF_CONTINUATION, "<else if block>");
    result = elseIfContinuationTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '{:' 'else' 'if' expression '}'
  public static boolean elseIfContinuationTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "elseIfContinuationTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ELSE_IF_CONTINUATION_TAG, "<else if continuation tag>");
    result = consumeTokens(builder, 3, START_INNER_MUSTACHE, ELSE, IF);
    pinned = result; // pin = 3
    result = result && report_error_(builder, expression(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // CODE_FRAGMENT
  public static boolean expression(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "expression")) return false;
    if (!nextTokenIs(builder, "<expression>", CODE_FRAGMENT)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, EXPRESSION, "<expression>");
    result = consumeToken(builder, CODE_FRAGMENT);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '@html' | '@debug'
  static boolean expressionPrefix(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "expressionPrefix")) return false;
    if (!nextTokenIs(builder, "", DEBUG_PREFIX, HTML_PREFIX)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, HTML_PREFIX);
    if (!result) result = consumeToken(builder, DEBUG_PREFIX);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // ifBlockOpening elseIfContinuation* elseContinuation? ifBlockClosingTag
  public static boolean ifBlock(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlock")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, IF_BLOCK, null);
    result = ifBlockOpening(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ifBlock_1(builder, level + 1));
    result = pinned && report_error_(builder, ifBlock_2(builder, level + 1)) && result;
    result = pinned && ifBlockClosingTag(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // elseIfContinuation*
  private static boolean ifBlock_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlock_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!elseIfContinuation(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "ifBlock_1", pos)) break;
    }
    return true;
  }

  // elseContinuation?
  private static boolean ifBlock_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlock_2")) return false;
    elseContinuation(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '{/' 'if' '}'
  public static boolean ifBlockClosingTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlockClosingTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, IF_BLOCK_CLOSING_TAG, "<if block closing tag>");
    result = consumeTokens(builder, 2, START_CLOSING_MUSTACHE, IF, END_MUSTACHE);
    pinned = result; // pin = 2
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // ifBlockOpeningTag scope
  public static boolean ifBlockOpening(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlockOpening")) return false;
    if (!nextTokenIs(builder, START_OPENING_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ifBlockOpeningTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, marker, IF_BLOCK_OPENING, result);
    return result;
  }

  /* ********************************************************** */
  // '{#' 'if' expression '}'
  public static boolean ifBlockOpeningTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ifBlockOpeningTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, IF_BLOCK_OPENING_TAG, "<if block opening tag>");
    result = consumeTokens(builder, 2, START_OPENING_MUSTACHE, IF);
    pinned = result; // pin = 2
    result = result && report_error_(builder, expression(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // '{' expressionPrefix? expression '}'
  public static boolean interpolation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interpolation")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INTERPOLATION, "<interpolation>");
    result = consumeToken(builder, START_MUSTACHE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, interpolation_1(builder, level + 1));
    result = pinned && report_error_(builder, expression(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

  // expressionPrefix?
  private static boolean interpolation_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interpolation_1")) return false;
    expressionPrefix(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !('{' | '{#' | "{:" | "{/" | HTML_FRAGMENT)
  static boolean mustache_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "mustache_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !mustache_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '{' | '{#' | "{:" | "{/" | HTML_FRAGMENT
  private static boolean mustache_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "mustache_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, START_MUSTACHE);
    if (!result) result = consumeToken(builder, START_OPENING_MUSTACHE);
    if (!result) result = consumeToken(builder, START_INNER_MUSTACHE);
    if (!result) result = consumeToken(builder, START_CLOSING_MUSTACHE);
    if (!result) result = consumeToken(builder, HTML_FRAGMENT);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // CODE_FRAGMENT
  public static boolean parameter(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "parameter")) return false;
    if (!nextTokenIs(builder, "<parameter>", CODE_FRAGMENT)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, PARAMETER, "<parameter>");
    result = consumeToken(builder, CODE_FRAGMENT);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // (block|interpolation|HTML_FRAGMENT)*
  static boolean privateScope(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "privateScope")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!privateScope_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "privateScope", pos)) break;
    }
    return true;
  }

  // block|interpolation|HTML_FRAGMENT
  private static boolean privateScope_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "privateScope_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = block(builder, level + 1);
    if (!result) result = interpolation(builder, level + 1);
    if (!result) result = consumeToken(builder, HTML_FRAGMENT);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // privateScope
  public static boolean scope(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scope")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, SCOPE, "<scope>");
    result = privateScope(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // privateScope
  static boolean svelteComponent(PsiBuilder builder, int level) {
    return privateScope(builder, level + 1);
  }

  /* ********************************************************** */
  // thenContinuationTag scope
  public static boolean thenContinuation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "thenContinuation")) return false;
    if (!nextTokenIs(builder, "<then block>", START_INNER_MUSTACHE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, THEN_CONTINUATION, "<then block>");
    result = thenContinuationTag(builder, level + 1);
    result = result && scope(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '{:' 'then' parameter '}'
  public static boolean thenContinuationTag(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "thenContinuationTag")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, THEN_CONTINUATION_TAG, "<then continuation tag>");
    result = consumeTokens(builder, 2, START_INNER_MUSTACHE, THEN);
    pinned = result; // pin = 2
    result = result && report_error_(builder, parameter(builder, level + 1));
    result = pinned && consumeToken(builder, END_MUSTACHE) && result;
    exit_section_(builder, level, marker, result, pinned, SvelteParser::mustache_recover);
    return result || pinned;
  }

}

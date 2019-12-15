// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.parsing.top;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static dev.blachut.svelte.lang.parsing.top.SvelteManualParsing.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class SvelteParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, null);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    result = parse_root_(type, builder);
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder) {
    return parse_root_(type, builder, 0);
  }

  static boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    return svelteComponent(builder, level + 1);
  }

  /* ********************************************************** */
  // (<<parseLazy>>|HTML_FRAGMENT)*
  static boolean privateScope(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "privateScope")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!privateScope_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "privateScope", pos)) break;
    }
    return true;
  }

  // <<parseLazy>>|HTML_FRAGMENT
  private static boolean privateScope_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "privateScope_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = parseLazy(builder, level + 1);
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

}

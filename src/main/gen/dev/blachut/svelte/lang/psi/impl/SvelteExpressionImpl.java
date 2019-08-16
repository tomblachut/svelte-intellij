// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import dev.blachut.svelte.lang.psi.SvelteExpression;
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl;
import dev.blachut.svelte.lang.psi.SvelteVisitor;
import org.jetbrains.annotations.NotNull;

public class SvelteExpressionImpl extends SveltePsiElementImpl implements SvelteExpression {

  public SvelteExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

}

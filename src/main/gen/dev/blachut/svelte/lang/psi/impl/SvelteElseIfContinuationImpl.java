// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl;
import dev.blachut.svelte.lang.psi.*;

public class SvelteElseIfContinuationImpl extends SveltePsiElementImpl implements SvelteElseIfContinuation {

  public SvelteElseIfContinuationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitElseIfContinuation(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public SvelteElseIfContinuationTag getElseIfContinuationTag() {
    return findNotNullChildByClass(SvelteElseIfContinuationTag.class);
  }

  @Override
  @NotNull
  public SvelteScope getScope() {
    return findNotNullChildByClass(SvelteScope.class);
  }

}

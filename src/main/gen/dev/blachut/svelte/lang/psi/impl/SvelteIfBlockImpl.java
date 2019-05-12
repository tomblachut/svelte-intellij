// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;
import dev.blachut.svelte.lang.psi.*;

public class SvelteIfBlockImpl extends SvelteBlockImpl implements SvelteIfBlock {

  public SvelteIfBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitIfBlock(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public SvelteElseContinuation getElseContinuation() {
    return findChildByClass(SvelteElseContinuation.class);
  }

  @Override
  @NotNull
  public List<SvelteElseIfContinuation> getElseIfContinuationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteElseIfContinuation.class);
  }

  @Override
  @Nullable
  public SvelteIfBlockClosingTag getIfBlockClosingTag() {
    return findChildByClass(SvelteIfBlockClosingTag.class);
  }

  @Override
  @NotNull
  public SvelteIfBlockOpening getIfBlockOpening() {
    return findNotNullChildByClass(SvelteIfBlockOpening.class);
  }

}

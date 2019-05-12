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

public class SvelteEachBlockImpl extends SvelteBlockImpl implements SvelteEachBlock {

  public SvelteEachBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitEachBlock(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public SvelteEachBlockClosingTag getEachBlockClosingTag() {
    return findChildByClass(SvelteEachBlockClosingTag.class);
  }

  @Override
  @NotNull
  public SvelteEachBlockOpening getEachBlockOpening() {
    return findNotNullChildByClass(SvelteEachBlockOpening.class);
  }

  @Override
  @Nullable
  public SvelteElseContinuation getElseContinuation() {
    return findChildByClass(SvelteElseContinuation.class);
  }

}

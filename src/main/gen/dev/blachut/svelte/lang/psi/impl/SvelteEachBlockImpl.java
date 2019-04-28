// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import dev.blachut.svelte.lang.psi.*;

public class SvelteEachBlockImpl extends ASTWrapperPsiElement implements SvelteEachBlock {

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
  @NotNull
  public SvelteEachBlockClosing getEachBlockClosing() {
    return findNotNullChildByClass(SvelteEachBlockClosing.class);
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

  @Override
  @NotNull
  public List<SvelteScope> getScopeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteScope.class);
  }

}

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

public class SvelteScopeImpl extends ASTWrapperPsiElement implements SvelteScope {

  public SvelteScopeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitScope(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<SvelteAwaitBlock> getAwaitBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteAwaitBlock.class);
  }

  @Override
  @NotNull
  public List<SvelteEachBlock> getEachBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteEachBlock.class);
  }

  @Override
  @NotNull
  public List<SvelteIfBlock> getIfBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteIfBlock.class);
  }

  @Override
  @NotNull
  public List<SvelteInterpolation> getInterpolationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteInterpolation.class);
  }

}

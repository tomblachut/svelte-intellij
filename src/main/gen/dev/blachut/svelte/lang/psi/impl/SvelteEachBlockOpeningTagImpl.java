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

public class SvelteEachBlockOpeningTagImpl extends SvelteOpeningTagImpl implements SvelteEachBlockOpeningTag {

  public SvelteEachBlockOpeningTagImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitEachBlockOpeningTag(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public SvelteExpression getExpression() {
    return findChildByClass(SvelteExpression.class);
  }

  @Override
  @Nullable
  public SvelteKeyExpression getKeyExpression() {
    return findChildByClass(SvelteKeyExpression.class);
  }

  @Override
  @NotNull
  public List<SvelteParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, SvelteParameter.class);
  }

}

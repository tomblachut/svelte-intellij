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

public class SvelteAwaitThenBlockOpeningTagImpl extends ASTWrapperPsiElement implements SvelteAwaitThenBlockOpeningTag {

  public SvelteAwaitThenBlockOpeningTagImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitAwaitThenBlockOpeningTag(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public SvelteExpression getExpression() {
    return findNotNullChildByClass(SvelteExpression.class);
  }

  @Override
  @Nullable
  public SvelteParameter getParameter() {
    return findChildByClass(SvelteParameter.class);
  }

}

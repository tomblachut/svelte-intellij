// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.blachut.svelte.lang.psi.SvelteTypes.*;
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl;
import dev.blachut.svelte.lang.psi.*;

public class SvelteScopeImpl extends SveltePsiElementImpl implements SvelteScope {

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

}

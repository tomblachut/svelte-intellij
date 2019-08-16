// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import dev.blachut.svelte.lang.psi.SvelteParameter;
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl;
import dev.blachut.svelte.lang.psi.SvelteVisitor;
import org.jetbrains.annotations.NotNull;

public class SvelteParameterImpl extends SveltePsiElementImpl implements SvelteParameter {

  public SvelteParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitParameter(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

}

// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface SvelteIfBlock extends PsiElement {

  @Nullable
  SvelteElseContinuation getElseContinuation();

  @NotNull
  List<SvelteElseIfContinuation> getElseIfContinuationList();

  @Nullable
  SvelteIfBlockClosing getIfBlockClosing();

  @NotNull
  SvelteIfBlockOpening getIfBlockOpening();

}

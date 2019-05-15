// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface SvelteEachBlock extends SvelteBlock {

  @Nullable
  SvelteEachBlockClosingTag getEachBlockClosingTag();

  @NotNull
  SvelteEachBlockOpening getEachBlockOpening();

  @Nullable
  SvelteElseContinuation getElseContinuation();

}

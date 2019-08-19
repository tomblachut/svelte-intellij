// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SvelteEachBlockOpeningTag extends SvelteOpeningTag {

  @Nullable
  SvelteExpression getExpression();

  @Nullable
  SvelteKeyExpression getKeyExpression();

  @NotNull
  List<SvelteParameter> getParameterList();

}

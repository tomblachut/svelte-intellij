// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.blocks.SvelteSnippetBlock

/**
 * Marks snippet functions as implicitly used when they are direct children of a component tag.
 *
 * In Svelte 5, snippets can be passed as props to child components:
 * ```svelte
 * <SnippetComponent>
 *   {#snippet title()}<h1>Hello</h1>{/snippet}
 * </SnippetComponent>
 * ```
 *
 * The child component renders it via `{@render title()}`, but in the parent file there is
 * no local `{@render}` reference, so the IDE would otherwise flag it as unused.
 */
class SvelteSnippetImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (element !is JSFunction) return false
    if (element.containingFile !is SvelteHtmlFile) return false

    val snippetBlock = element.parentOfType<SvelteSnippetBlock>() ?: return false
    val parentTag = snippetBlock.parentOfType<XmlTag>() ?: return false

    return isSvelteComponentTag(parentTag.name)
  }

  override fun isImplicitRead(element: PsiElement): Boolean = false
  override fun isImplicitWrite(element: PsiElement): Boolean = false
}

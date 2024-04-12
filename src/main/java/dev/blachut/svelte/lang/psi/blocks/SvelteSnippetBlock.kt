package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode

class SvelteSnippetBlock(node: ASTNode) : SvelteBlock(node, "snippet")

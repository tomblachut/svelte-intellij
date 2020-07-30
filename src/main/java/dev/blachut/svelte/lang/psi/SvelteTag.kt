package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType

interface SvelteTag : JSElement {
    val type: IElementType
}

class SvelteInitialTag(type: IElementType, text: CharSequence) : LazyParseablePsiElement(type, text), SvelteTag, JSEmbeddedContent {
    override val type: IElementType get() = elementType

    override fun accept(visitor: PsiElementVisitor) {
        when (visitor) {
            is SvelteVisitor -> visitor.visitInitialTag(this)
            is JSElementVisitor -> visitor.visitJSEmbeddedContent(this)
            else -> super.accept(visitor)
        }
    }

    override fun toString(): String {
        return "SvelteInitialTag($elementType)"
    }
}

class SvelteEndTag(node: ASTNode) : SveltePsiElement(node), SvelteTag {
    override val type: IElementType get() = node.elementType

    override fun toString(): String {
        return super.toString() + "(" + node.elementType.toString() + ")"
    }
}

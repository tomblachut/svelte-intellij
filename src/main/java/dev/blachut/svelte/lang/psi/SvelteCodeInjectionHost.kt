package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor

open class SvelteCodeInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, InjectionBackgroundSuppressor {
    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return SvelteLiteralTextEscaper(this)
    }

    override fun isValidHost(): Boolean {
        return true
    }
}


class SvelteLiteralTextEscaper(host: SvelteCodeInjectionHost) : LiteralTextEscaper<SvelteCodeInjectionHost>(host) {
    override fun isOneLine(): Boolean = false

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        return rangeInsideHost.startOffset + offsetInDecoded
    }

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(myHost.text, rangeInsideHost.startOffset, rangeInsideHost.endOffset)
        return true
    }
}
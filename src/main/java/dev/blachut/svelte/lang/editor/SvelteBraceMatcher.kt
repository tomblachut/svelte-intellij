package dev.blachut.svelte.lang.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> {
        return arrayOf(
            BracePair(SvelteTypes.START_MUSTACHE, SvelteTypes.END_MUSTACHE, true),
            BracePair(JSTokenTypes.LBRACE, JSTokenTypes.RBRACE, true)
        )
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }
}

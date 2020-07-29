package dev.blachut.svelte.lang.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> {
        return arrayOf(BracePair(SvelteTokenTypes.START_MUSTACHE, SvelteTokenTypes.END_MUSTACHE, true))
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }
}

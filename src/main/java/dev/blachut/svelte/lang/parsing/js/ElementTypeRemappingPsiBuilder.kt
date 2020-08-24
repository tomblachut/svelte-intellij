package dev.blachut.svelte.lang.parsing.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.impl.DelegateMarker
import com.intellij.lang.impl.PsiBuilderAdapter
import com.intellij.psi.tree.IElementType

abstract class ElementTypeRemappingPsiBuilder(delegate: PsiBuilder) : PsiBuilderAdapter(delegate) {
    abstract fun remapElementType(type: IElementType): IElementType

    override fun mark(): PsiBuilder.Marker {
        return RemappingMarker(super.mark())
    }

    inner class RemappingMarker(delegate: PsiBuilder.Marker) : DelegateMarker(delegate) {
        override fun done(type: IElementType) {
            super.done(remapElementType(type))
        }

        override fun collapse(type: IElementType) {
            super.collapse(remapElementType(type))
        }

        override fun doneBefore(type: IElementType, before: PsiBuilder.Marker) {
            super.doneBefore(remapElementType(type), before)
        }

        override fun doneBefore(type: IElementType, before: PsiBuilder.Marker, errorMessage: String) {
            super.doneBefore(remapElementType(type), before, errorMessage)
        }
    }
}

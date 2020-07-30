package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.util.XmlPsiUtil
import dev.blachut.svelte.lang.psi.SveltePsiElement

class SvelteFragment(node: ASTNode) : SveltePsiElement(node), XmlElement {
    override fun processElements(processor: PsiElementProcessor<PsiElement>, place: PsiElement?): Boolean {
        return XmlPsiUtil.processXmlElements(this, processor, false)
    }
}

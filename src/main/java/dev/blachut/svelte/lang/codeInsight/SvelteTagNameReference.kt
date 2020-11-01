package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.xml.TagNameReference

class SvelteTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) :
    TagNameReference(nameElement, startTagFlag), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val results = this.multiResolve(false)
        return if (results.isNotEmpty()) results[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val tag = tagElement ?: return emptyArray()

        val resolver = ResolveCache.PolyVariantResolver<SvelteTagNameReference> { _, _ ->
            val sink = ResolveResultSink(tag, tag.name, false, incompleteCode)
            val processor = SinkResolveProcessor(tag.name, tag, sink)
            JSResolveUtil.treeWalkUp(processor, tag, tag, tag, tag.containingFile)

            processor.resultsAsResolveResults
        }

        return JSResolveUtil.resolve(tag.containingFile, this, resolver, incompleteCode)
    }
}

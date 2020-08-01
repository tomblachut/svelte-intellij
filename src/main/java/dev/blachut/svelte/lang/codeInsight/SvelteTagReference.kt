package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.xml.XmlTag

class SvelteTagReference(val tag: XmlTag) : PsiPolyVariantReferenceBase<XmlTag>(tag) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val resolver = ResolveCache.PolyVariantResolver<SvelteTagReference> { _, _ ->
            val sink = ResolveResultSink(tag, tag.name, false, incompleteCode)
            val processor = SinkResolveProcessor(tag.name, tag, sink)
            JSResolveUtil.treeWalkUp(processor, tag, tag, tag, tag.containingFile)

            processor.resultsAsResolveResults
        }

        return JSResolveUtil.resolve(tag.containingFile, this, resolver, incompleteCode)
    }
}

package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.xml.TagNameReference
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

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

    companion object {
        fun resolveComponentFile(tag: SvelteHtmlTag): SvelteHtmlFile? {
            val import = tag.reference?.resolve()
            if (import is ES6ImportedBinding && !import.isNamespaceImport) {
                // TODO verify if below comment is still valid
                // com.intellij.javascript.JSFileReference.IMPLICIT_EXTENSIONS doesn't include .svelte
                // probably because of that following call returns null
                // val componentFile = declaration.findReferencedElements().firstOrNull()
                val componentFile = import.declaration?.fromClause?.resolveReferencedElements()?.firstOrNull()

                if (componentFile is SvelteHtmlFile) {
                    return componentFile
                }
            }

            return null
        }
    }
}

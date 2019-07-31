package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext


class SvelteReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val pattern = PlatformPatterns.psiElement(JSReferenceExpression::class.java).and(FilterPattern(object : ElementFilter {
            override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
                if (element is JSReferenceExpression) {
                    // TODO: check if already declared
                    return element.parent !is JSDefinitionExpression
                }
                return false
            }

            override fun isClassAcceptable(hintClass: Class<*>): Boolean {
                return true
            }
        }))
        registrar.registerReferenceProvider(pattern, object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                return arrayOf(SvelteLabeledReference(element, element.textRange))
            }
        })
    }
}


class SvelteLabeledReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val embeddedContent = PsiTreeUtil.findFirstParent(element) { psiElement -> psiElement is JSEmbeddedContent }
        val labeledStatements = PsiTreeUtil.findChildrenOfType(embeddedContent, JSLabeledStatement::class.java)
        labeledStatements.forEach {
            val definitions = PsiTreeUtil.findChildrenOfType(it, JSDefinitionExpression::class.java)
            definitions.forEach { definition ->
                if (definition.name == element.text) {
                    return arrayOf(JSResolveResult(definition))
                }
            }
        }
        return ResolveResult.EMPTY_ARRAY
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }
}

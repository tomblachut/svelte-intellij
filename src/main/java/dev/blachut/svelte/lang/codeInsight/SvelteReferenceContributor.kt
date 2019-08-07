package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

object DeclarationFinder {
    fun hasDeclaration(element: JSReferenceExpressionImpl?): Boolean {
        element ?: return false
        val cachedResults = ResolveCache.getInstance(element.project).resolveWithCaching(element, JSReferenceExpressionResolver(element, false), true, false)
        return cachedResults.find {
            it.element is JSVariable
        } != null
    }
}

class SvelteReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val pattern = PlatformPatterns.psiElement(JSReferenceExpression::class.java).and(FilterPattern(object : ElementFilter {
            override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
                if (element is JSReferenceExpressionImpl) {
                    return !DeclarationFinder.hasDeclaration(element)
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
        val elementDefinition = PsiTreeUtil.findFirstParent(element) { psiElement -> psiElement is JSDefinitionExpression }
        val labeledStatements = PsiTreeUtil.findChildrenOfType(embeddedContent, JSLabeledStatement::class.java)
        labeledStatements.forEach {
            val definitions = PsiTreeUtil.findChildrenOfType(it, JSDefinitionExpression::class.java)
            definitions.filter { definition -> definition != elementDefinition }.forEach { definition ->
                if (definition.name == element.text && !DeclarationFinder.hasDeclaration(PsiTreeUtil.findChildOfType(definition, JSReferenceExpressionImpl::class.java))) {
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

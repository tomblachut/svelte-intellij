package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.TypedJSReferenceChecker
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSEmbeddedContentImpl

/**
 * No TypeScript counterpart because right now JSEmbeddedContent is JS-only
 */
class SvelteJSReferenceChecker(reporter: JSProblemReporter<*>) : TypedJSReferenceChecker(reporter) {
    override fun addCreateFromUsageFixes(node: JSReferenceExpression,
                                         resolveResults: Array<out ResolveResult>,
                                         fixes: MutableList<LocalQuickFix>,
                                         inTypeContext: Boolean,
                                         ecma: Boolean): Boolean {
        if (node.parentOfType<JSEmbeddedContent>() is SvelteJSEmbeddedContentImpl) {
            // only override Svelte expressions and blocks
            return super.addCreateFromUsageFixes(node, resolveResults, fixes, inTypeContext, ecma)
        }

        fixes.add(SvelteCreateJSVariableIntentionAction(node))

        return inTypeContext
    }

    override fun addFunctionFixes(node: JSReferenceExpression,
                                  fixes: MutableList<LocalQuickFix>,
                                  refName: String?,
                                  dialect: DialectOptionHolder?,
                                  qualifier: JSExpression?) {
        if (node.parentOfType<JSEmbeddedContent>() is SvelteJSEmbeddedContentImpl) {
            // only override Svelte expressions and blocks
            return super.addFunctionFixes(node, fixes, refName, dialect, qualifier)
        }

        fixes.add(SvelteCreateJSFunctionIntentionAction(node))
    }
}

class SvelteCreateJSVariableIntentionAction(reference: JSReferenceExpression)
    : CreateJSVariableIntentionAction(reference.referenceName, false, false, false) {
    private val myRefExpressionPointer = SmartPointerManager.getInstance(reference.project).createSmartPsiElementPointer(reference)

    override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
        return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
    }

    override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
        // assumes psiElement isn't inside script tag
        val containingFile = file as? SvelteHtmlFile ?: return
        val embeddedContent = prepareInstanceScriptContent(containingFile)

        doApplyFix(project, embeddedContent, file, editor)
    }
}

class SvelteCreateJSFunctionIntentionAction(reference: JSReferenceExpression)
    : CreateJSFunctionIntentionAction(reference.referenceName, false, false, false) {
    private val myRefExpressionPointer = SmartPointerManager.getInstance(reference.project).createSmartPsiElementPointer(reference)

    override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
        return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
    }

    override fun applyFix(project: Project?, psiElement: PsiElement?, file: PsiFile, editor: Editor?) {
        // assumes psiElement isn't inside script tag
        val containingFile = file as? SvelteHtmlFile ?: return
        val embeddedContent = prepareInstanceScriptContent(containingFile)

        doApplyFix(project, embeddedContent, file, editor)
    }
}

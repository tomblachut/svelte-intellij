// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.ecmascript6.validation.ES6AnalysisHandlersFactory
import com.intellij.lang.ecmascript6.validation.ES6AnnotatingVisitor
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.lang.javascript.validation.JSKeywordHighlighterVisitor
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.codeInsight.SvelteJSReferenceChecker
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil

class SvelteJSAnalysisHandlersFactory : ES6AnalysisHandlersFactory() {
    override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker {
        return SvelteJSReferenceChecker(reporter)
    }

    override fun createKeywordHighlighterVisitor(
        holder: HighlightInfoHolder,
        dialectOptionHolder: DialectOptionHolder
    ): JSKeywordHighlighterVisitor {
        return SvelteKeywordHighlighterVisitor(holder)
    }

    override fun createAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder): JSAnnotatingVisitor {
        return object : ES6AnnotatingVisitor(psiElement, holder) {
            override fun visitJSLabeledStatement(node: JSLabeledStatement) {
                if (node.label != SvelteReactiveDeclarationsUtil.REACTIVE_LABEL) {
                    super.visitJSLabeledStatement(node)
                }
            }
        }
    }
}

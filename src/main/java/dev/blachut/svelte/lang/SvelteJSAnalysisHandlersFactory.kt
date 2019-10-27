// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.ecmascript6.validation.ES6AnalysisHandlersFactory
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSReferenceInspectionProblemReporter
import com.intellij.lang.javascript.validation.TypedJSReferenceChecker

class SvelteJSAnalysisHandlersFactory : ES6AnalysisHandlersFactory() {
    override fun getReferenceChecker(reporter: JSReferenceInspectionProblemReporter): JSReferenceChecker {
        return object : TypedJSReferenceChecker(reporter) {
            override fun checkRefExpression(node: JSReferenceExpression) {
                val isPropsReference = JSSymbolUtil.isAccurateReferenceExpressionName(node, "\$\$props")
                if (!isPropsReference) {
                    super.checkRefExpression(node)
                }
            }
        }
    }
}

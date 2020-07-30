// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.ecmascript6.validation.ES6AnalysisHandlersFactory
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.validation.JSKeywordHighlighterVisitor
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSReferenceInspectionProblemReporter
import com.intellij.lang.javascript.validation.TypedJSReferenceChecker

class SvelteJSAnalysisHandlersFactory : ES6AnalysisHandlersFactory() {
    override fun getReferenceChecker(reporter: JSReferenceInspectionProblemReporter): JSReferenceChecker {
        return TypedJSReferenceChecker(reporter)
    }

    override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder, dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor {
        return SvelteKeywordHighlighterVisitor(holder)
    }
}

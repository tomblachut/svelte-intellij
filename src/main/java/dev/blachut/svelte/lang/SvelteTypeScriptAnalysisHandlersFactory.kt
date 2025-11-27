// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatingVisitor
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil

private class SvelteTypeScriptAnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {

  override fun createAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder): JSAnnotatingVisitor {
    return object : TypeScriptAnnotatingVisitor(psiElement, holder) {
      override fun visitJSLabeledStatement(node: JSLabeledStatement) {
        if (node.label != SvelteReactiveDeclarationsUtil.REACTIVE_LABEL) {
          super.visitJSLabeledStatement(node)
        }
      }
    }
  }
}
